package bq_standard.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.quests.tasks.advanced.IContainerTask;
import betterquesting.quests.tasks.advanced.IProgressionTask;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.ItemComparison;
import betterquesting.utils.JsonHelper;
import bq_standard.client.gui.editors.GuiRetrievalEditor;
import bq_standard.client.gui.tasks.GuiTaskRetrieval;
import bq_standard.core.BQ_Standard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class TaskRetrieval extends TaskBase implements IContainerTask, IProgressionTask<int[]>
{
	public ArrayList<BigItemStack> requiredItems = new ArrayList<BigItemStack>();
	public HashMap<UUID, int[]> userProgress = new HashMap<UUID, int[]>();
	boolean partialMatch = true;
	boolean ignoreNBT = false;
	public boolean consume = true;
	public boolean autoConsume = false;
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.retrieval";
	}
	
	@Override
	public void Update(QuestInstance quest, EntityPlayer player)
	{
		if(player.ticksExisted%60 == 0 && !QuestDatabase.editMode) // Every ~10 seconds auto detect this quest as long as it isn't consuming items
		{
			if(!consume || autoConsume)
			{
				Detect(quest, player);
			} else
			{
				boolean flag = true;
				
				int[] totalProgress = quest == null || !quest.globalQuest? GetPartyProgress(player.getUniqueID()) : GetGlobalProgress();
				for(int j = 0; j < requiredItems.size(); j++)
				{
					BigItemStack rStack = requiredItems.get(j);
					
					if(rStack == null || totalProgress[j] >= rStack.stackSize)
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
		if(player.inventory == null || this.isComplete(player.getUniqueID()))
		{
			return;
		}
		
		int[] progress = GetUserProgress(player.getUniqueID());
		
		for(int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			for(int j = 0; j < requiredItems.size(); j++)
			{
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(stack == null)
				{
					break;
				}
				
				BigItemStack rStack = requiredItems.get(j);
				
				if(rStack == null || progress[j] >= rStack.stackSize)
				{
					continue;
				}

				int remaining = rStack.stackSize - progress[j];
				
				if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch) || ItemComparison.OreDictionaryMatch(rStack.oreDict, rStack.GetTagCompound(), stack, !ignoreNBT, partialMatch))
				{
					if(consume)
					{
						ItemStack removed = player.inventory.decrStackSize(i, remaining);
						progress[j] += removed.stackSize;
					} else
					{
						progress[j] += Math.min(remaining, stack.stackSize);
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
		
		for(int j = 0; j < requiredItems.size(); j++)
		{
			BigItemStack rStack = requiredItems.get(j);
			
			if(rStack == null || totalProgress[j] >= rStack.stackSize)
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

	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("partialMatch", partialMatch);
		json.addProperty("ignoreNBT", ignoreNBT);
		json.addProperty("consume", consume);
		json.addProperty("autoConsume", autoConsume);
		
		JsonArray itemArray = new JsonArray();
		for(BigItemStack stack : this.requiredItems)
		{
			itemArray.add(JsonHelper.ItemStackToJson(stack, new JsonObject()));
		}
		json.add("requiredItems", itemArray);
	}

	@Override
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
		partialMatch = JsonHelper.GetBoolean(json, "partialMatch", partialMatch);
		ignoreNBT = JsonHelper.GetBoolean(json, "ignoreNBT", ignoreNBT);
		consume = JsonHelper.GetBoolean(json, "consume", true);
		autoConsume = JsonHelper.GetBoolean(json, "autoConsume", false);
		
		requiredItems = new ArrayList<BigItemStack>();
		for(JsonElement entry : JsonHelper.GetArray(json, "requiredItems"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			BigItemStack item = JsonHelper.JsonToItemStack(entry.getAsJsonObject());
			
			if(item != null)
			{
				requiredItems.add(item);
			} else
			{
				continue;
			}
		}
		
		if(json.has("userProgress"))
		{
			jMig = json;
		}
	}
	
	JsonObject jMig = null;
	
	@Override
	public void readProgressFromJson(JsonObject json)
	{
		super.readProgressFromJson(json);
		
		if(jMig != null)
		{
			json = jMig;
			jMig = null;
		}
		
		userProgress = new HashMap<UUID,int[]>();
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
			
			int[] data = new int[requiredItems.size()];
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
	public void writeProgressToJson(JsonObject json)
	{
		super.writeProgressToJson(json);
		
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
	public float GetParticipation(UUID uuid)
	{
		if(requiredItems.size() <= 0)
		{
			return 1F;
		}
		
		float total = 0F;
		
		int[] progress = GetUserProgress(uuid);
		for(int i = 0; i < requiredItems.size(); i++)
		{
			BigItemStack rStack = requiredItems.get(i);
			total += progress[i] / (float)rStack.stackSize;
		}
		
		return total / (float)requiredItems.size();
	}

	@Override
	public GuiEmbedded getGui(QuestInstance quest, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskRetrieval(quest, this, screen, posX, posY, sizeX, sizeY);
	}

	@Override
	public boolean canAcceptFluid(UUID owner, Fluid fluid)
	{
		return false;
	}

	@Override
	public boolean canAcceptItem(UUID owner, ItemStack stack)
	{
		if(owner == null || stack == null || !consume || isComplete(owner) || requiredItems.size() <= 0)
		{
			return false;
		}
		
		int[] progress = GetUserProgress(owner);
		
		for(int j = 0; j < requiredItems.size(); j++)
		{
			BigItemStack rStack = requiredItems.get(j);
			
			if(rStack == null || progress[j] >= rStack.stackSize)
			{
				continue;
			}
			
			if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch) || ItemComparison.OreDictionaryMatch(rStack.oreDict, rStack.GetTagCompound(), stack, !ignoreNBT, partialMatch))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public FluidStack submitFluid(UUID owner, FluidStack fluid)
	{
		return fluid;
	}

	@Override
	public void submitItem(UUID owner, Slot input, Slot output)
	{
		ItemStack stack = input.getStack();
		
		if(owner == null || stack == null || !consume || isComplete(owner))
		{
			return;
		}
		
		int[] progress = userProgress.get(owner);
		progress = progress == null || progress.length != requiredItems.size()? new int[requiredItems.size()] : progress;
		
		for(int j = 0; j < requiredItems.size(); j++)
		{
			if(stack == null)
			{
				break;
			}
			
			BigItemStack rStack = requiredItems.get(j);
			
			if(rStack == null || progress[j] >= rStack.stackSize)
			{
				continue;
			}

			int remaining = rStack.stackSize - progress[j];
			
			if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch) || ItemComparison.OreDictionaryMatch(rStack.oreDict, rStack.GetTagCompound(), stack, !ignoreNBT, partialMatch))
			{
				int removed = Math.min(stack.stackSize, remaining);
				stack.stackSize -= removed;
				progress[j] += removed;
				
				if(stack.stackSize <= 0)
				{
					break;
				}
			}
		}
		
		userProgress.put(owner, progress);
		
		boolean flag = true;
		
		for(int j = 0; j < requiredItems.size(); j++)
		{
			BigItemStack rStack = requiredItems.get(j);
			
			if(rStack == null || progress[j] >= rStack.stackSize)
			{
				continue;
			}
			
			flag = false;
			break;
		}
		
		if(flag)
		{
			setCompletion(owner, true);
		}
		
		if(stack == null || stack.stackSize <= 0)
		{
			input.putStack(null);
		} else
		{
			input.putStack(stack);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen GetEditor(GuiScreen parent, JsonObject data)
	{
		return new GuiRetrievalEditor(parent, data);
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
		return progress == null || progress.length != requiredItems.size()? new int[requiredItems.size()] : progress;
	}

	@Override
	public int[] GetPartyProgress(UUID uuid)
	{
		int[] total = new int[requiredItems.size()];
		
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
		int[] total = new int[requiredItems.size()];
		
		for(int[] up : userProgress.values())
		{
			if(up == null)
			{
				continue;
			}
			
			int[] progress = up.length != requiredItems.size()? new int[requiredItems.size()] : up;
			
			for(int i = 0; i < progress.length; i++)
			{
				total[i] += progress[i];
			}
		}
		
		return total;
	}
}
