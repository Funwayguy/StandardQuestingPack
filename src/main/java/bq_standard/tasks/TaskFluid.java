package bq_standard.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyManager;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.quests.tasks.advanced.IContainerTask;
import betterquesting.quests.tasks.advanced.IProgressionTask;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import bq_standard.client.gui.tasks.GuiTaskFluid;
import bq_standard.core.BQ_Standard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@SuppressWarnings("deprecation") // We are intentionally supporting mods using deprecated fluid handlers
public class TaskFluid extends TaskBase implements IContainerTask, IProgressionTask<int[]>
{
	public ArrayList<FluidStack> requiredFluids = new ArrayList<FluidStack>();
	public HashMap<UUID, int[]> userProgress = new HashMap<UUID, int[]>();
	public boolean consume = true;
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.fluid";
	}
	
	@Override
	public void Update(QuestInstance quest, EntityPlayer player)
	{
		if(player.ticksExisted%60 == 0 && !QuestDatabase.editMode)
		{
			if(!consume)
			{
				Detect(quest, player);
			} else
			{
				boolean flag = true;
				
				int[] totalProgress = quest == null || !quest.globalQuest? GetPartyProgress(player.getUniqueID()) : GetGlobalProgress();
				for(int j = 0; j < requiredFluids.size(); j++)
				{
					FluidStack rStack = requiredFluids.get(j);
					
					if(rStack == null || totalProgress[j] >= rStack.amount)
					{
						continue;
					}
					
					flag = false;
					break;
				}
				
				if(flag)
				{
					setCompletion(player.getUniqueID(), true);
				}
			}
		}
	}

	@Override
	public void Detect(QuestInstance quest, EntityPlayer player)
	{
		if(player.inventory == null || isComplete(player.getUniqueID()))
		{
			return;
		}
		
		int[] progress = GetUserProgress(player.getUniqueID());
		
		for(int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			for(int j = 0; j < requiredFluids.size(); j++)
			{
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(stack == null || FluidContainerRegistry.isEmptyContainer(stack))
				{
					break;
				}
				
				FluidStack rStack = requiredFluids.get(j);
				
				if(rStack == null || progress[j] >= rStack.amount)
				{
					continue;
				}
				
				int remaining = rStack.amount - progress[j];
				
				if(rStack.isFluidEqual(stack))
				{
					if(consume)
					{
						FluidStack fluid = this.getFluid(player, i, true, remaining);
						progress[j] += Math.min(remaining, fluid == null? 0 : fluid.amount);
					} else
					{
						FluidStack fluid = this.getFluid(player, i, false, remaining);
						progress[j] += Math.min(remaining, fluid == null? 0 : fluid.amount);
					}
				}
			}
		}
		
		boolean flag = true;
		int[] totalProgress = progress;
		
		if(consume)
		{
			SetUserProgress(player.getUniqueID(), progress);
			totalProgress = quest == null || !quest.globalQuest? GetPartyProgress(player.getUniqueID()) : GetGlobalProgress();
		}
		
		for(int j = 0; j < requiredFluids.size(); j++)
		{
			FluidStack rStack = requiredFluids.get(j);
			
			if(rStack == null || totalProgress[j] >= rStack.amount)
			{
				continue;
			}
			
			flag = false;
			break;
		}
		
		if(flag)
		{
			setCompletion(player.getUniqueID(), true);
		}
	}
	
	/**
	 * Returns the fluid drained (or can be drained) up to the specified amount
	 */
	public FluidStack getFluid(EntityPlayer player, int slot, boolean drain, int amount)
	{
		ItemStack stack = player.inventory.getStackInSlot(slot);
		
		if(stack == null || amount <= 0)
		{
			return null;
		}
		
		if(stack.getItem() instanceof IFluidContainerItem)
		{
			IFluidContainerItem container = (IFluidContainerItem)stack.getItem();
			
			return container.drain(stack, amount, drain);
		} else
		{
			FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
			int tmp1 = fluid.amount;
			int tmp2 = 1;
			while(fluid.amount < amount && tmp2 < stack.stackSize)
			{
				tmp2++;
				fluid.amount += tmp1;
			}
			
			if(drain)
			{
				for(; tmp2 > 0; tmp2--)
				{
					ItemStack empty = FluidContainerRegistry.drainFluidContainer(stack);
					player.inventory.decrStackSize(slot, 1);
					
					if(!player.inventory.addItemStackToInventory(empty))
					{
						player.dropPlayerItemWithRandomChoice(empty, false);
					}
				}
			}
			
			return fluid;
		}
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("consume", consume);
		
		JsonArray itemArray = new JsonArray();
		for(FluidStack stack : this.requiredFluids)
		{
			itemArray.add(NBTConverter.NBTtoJSON_Compound(stack.writeToNBT(new NBTTagCompound()), new JsonObject()));
		}
		json.add("requiredFluids", itemArray);
		
		JsonArray progArray = new JsonArray();
		for(Entry<UUID,int[]> entry : userProgress.entrySet())
		{
			JsonObject pJson = new JsonObject();
			pJson.addProperty("uuid", entry.getKey().toString());
			JsonArray pArray = new JsonArray();
			for(int i : entry.getValue())
			{
				pArray.add(new JsonPrimitive(i));
			}
			pJson.add("data", pArray);
			progArray.add(pJson);
		}
		json.add("userProgress", progArray);
	}

	@Override
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
		consume = JsonHelper.GetBoolean(json, "consume", true);
		
		requiredFluids = new ArrayList<FluidStack>();
		for(JsonElement entry : JsonHelper.GetArray(json, "requiredFluids"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			FluidStack fluid = JsonHelper.JsonToFluidStack(entry.getAsJsonObject());
			
			if(fluid != null)
			{
				requiredFluids.add(fluid);
			} else
			{
				continue;
			}
		}
		
		userProgress = new HashMap<UUID, int[]>();
		for(JsonElement entry : JsonHelper.GetArray(json, "userProgress"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			UUID uuid;
			try
			{
				uuid = UUID.fromString(JsonHelper.GetString(entry.getAsJsonObject(), "uuid", ""));
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load user progress for task", e);
				continue;
			}
			
			int[] data = new int[requiredFluids.size()];
			JsonArray dJson = JsonHelper.GetArray(entry.getAsJsonObject(), "data");
			for(int i = 0; i < data.length && i < dJson.size(); i++)
			{
				try
				{
					data[i] = dJson.get(i).getAsInt();
				} catch(Exception e)
				{
					BQ_Standard.logger.log(Level.ERROR, "Incorrect task progress format", e);
				}
			}
			
			userProgress.put(uuid, data);
		}
	}

	@Override
	public void ResetProgress(UUID uuid)
	{
		super.ResetProgress(uuid);
		userProgress.remove(uuid);
	}

	@Override
	public void ResetAllProgress()
	{
		super.ResetAllProgress();
		userProgress = new HashMap<UUID,int[]>();
	}

	@Override
	public GuiEmbedded getGui(QuestInstance quest, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskFluid(quest, this, screen, posX, posY, sizeX, sizeY);
	}

	@Override
	public boolean canAcceptFluid(UUID owner, Fluid fluid)
	{
		if(owner == null || fluid == null || !consume || isComplete(owner) || requiredFluids.size() <= 0)
		{
			return false;
		}
		
		int[] progress = GetUserProgress(owner);
		
		for(int j = 0; j < requiredFluids.size(); j++)
		{
			FluidStack rStack = requiredFluids.get(j);
			
			if(rStack == null || progress[j] >= rStack.amount)
			{
				continue;
			}
			
			if(rStack.getFluid().equals(fluid))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean canAcceptItem(UUID owner, ItemStack item)
	{
		if(owner == null || item == null || !consume || isComplete(owner) || requiredFluids.size() <= 0)
		{
			return false;
		}
		
		if(item.getItem() instanceof IFluidContainerItem)
		{
			FluidStack contents = ((IFluidContainerItem)item.getItem()).getFluid(item);
			
			return contents != null && this.canAcceptFluid(owner, contents.getFluid());
		} else if(FluidContainerRegistry.isFilledContainer(item))
		{
			FluidStack contents = FluidContainerRegistry.getFluidForFilledItem(item);
			
			return contents != null && this.canAcceptFluid(owner, contents.getFluid());
		}
		
		return false;
	}

	@Override
	public FluidStack submitFluid(UUID owner, FluidStack fluid)
	{
		if(owner == null || fluid == null || fluid.amount <= 0 || !consume || isComplete(owner) || requiredFluids.size() <= 0)
		{
			return fluid;
		}
		
		int[] progress = GetUserProgress(owner);
		
		for(int j = 0; j < requiredFluids.size(); j++)
		{
			FluidStack rStack = requiredFluids.get(j);
			
			if(rStack == null || progress[j] >= rStack.amount)
			{
				continue;
			}
			
			int remaining = rStack.amount - progress[j];
			
			if(rStack.isFluidEqual(fluid))
			{
				int removed = Math.min(fluid.amount, remaining);
				progress[j] += removed;
				fluid.amount -= removed;
				
				if(fluid.amount <= 0)
				{
					fluid = null;
					break;
				}
			}
		}
		
		if(consume)
		{
			SetUserProgress(owner, progress);
		}
		
		return fluid;
	}

	@Override
	public void submitItem(UUID owner, Slot input, Slot output)
	{
		ItemStack item = input.getStack();
		
		if(item == null)
		{
			return;
		}
		
		item = item.copy(); // Prevents issues with stack filling/draining
		item.stackSize = 1; // Decrease input stack by 1 when drain has been confirmed
		
		if(item.getItem() instanceof IFluidContainerItem)
		{
			IFluidContainerItem container = (IFluidContainerItem)item.getItem();
			FluidStack fluid = container.getFluid(item);
			int amount = fluid.amount;
			fluid = submitFluid(owner, fluid);
			container.drain(item, fluid == null? amount : amount - fluid.amount, true);
			input.decrStackSize(1);
			output.putStack(item);
			return;
			
		} else
		{
			FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(item);
			
			if(fluid != null)
			{
				submitFluid(owner, fluid);
				output.putStack(FluidContainerRegistry.drainFluidContainer(item));
				input.decrStackSize(1);
			}
		}
	}

	@Override
	public void SetUserProgress(UUID uuid, int[] progress)
	{
		userProgress.put(uuid, progress);
	}

	@Override
	public int[] GetUserProgress(UUID uuid)
	{
		int[] progress = userProgress.get(uuid);
		return progress == null || progress.length != requiredFluids.size()? new int[requiredFluids.size()] : progress;
	}

	@Override
	public int[] GetPartyProgress(UUID uuid)
	{
		int[] total = new int[requiredFluids.size()];
		
		PartyInstance party = PartyManager.GetParty(uuid);
		
		if(party == null)
		{
			return GetUserProgress(uuid);
		} else
		{
			for(PartyMember mem : party.GetMembers())
			{
				if(mem != null && mem.GetPrivilege() <= 0)
				{
					continue;
				}

				int[] progress = GetUserProgress(mem.userID);
				
				for(int i = 0; i < progress.length; i++)
				{
					total[i] += progress[i];
				}
			}
		}
		
		return total;
	}

	@Override
	public int[] GetGlobalProgress()
	{
		int[] total = new int[requiredFluids.size()];
		
		for(int[] up : userProgress.values())
		{
			if(up == null)
			{
				continue;
			}
			
			int[] progress = up.length != requiredFluids.size()? new int[requiredFluids.size()] : up;
			
			for(int i = 0; i < progress.length; i++)
			{
				total[i] += progress[i];
			}
		}
		
		return total;
	}
}
