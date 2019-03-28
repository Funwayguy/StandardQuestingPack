package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.IFluidTask;
import betterquesting.api.questing.tasks.IItemTask;
import betterquesting.api.questing.tasks.IProgression;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import bq_standard.client.gui.tasks.PanelTaskFluid;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskFluid;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.Map.Entry;

public class TaskFluid implements ITaskInventory, IFluidTask, IItemTask, IProgression<int[]>
{
	private final List<UUID> completeUsers = new ArrayList<>();
	public final List<FluidStack> requiredFluids = new ArrayList<>();
	public final Map<UUID, int[]> userProgress = new HashMap<>();
	//public boolean partialMatch = true; // Not many ideal ways of implementing this with fluid handlers
	public boolean ignoreNbt = false;
	public boolean consume = true;
	public boolean groupDetect = false;
	public boolean autoConsume = false;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskFluid.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.fluid";
	}
	
	@Override
	public boolean isComplete(UUID uuid)
	{
		return completeUsers.contains(uuid);
	}
	
	@Override
	public void setComplete(UUID uuid)
	{
		if(!completeUsers.contains(uuid))
		{
			completeUsers.add(uuid);
		}
	}
	
	@Override
	public void onInventoryChange(@Nonnull DBEntry<IQuest> quest, @Nonnull EntityPlayer player)
	{
        if(!consume || autoConsume)
        {
            detect(player, quest.getValue());
        }
	}

	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		if(player.inventory == null || isComplete(playerID)) return;
		
		int[] progress = getUsersProgress(playerID);
		boolean updated = false;
		
        if(!consume)
        {
            if(groupDetect) // Reset all detect progress
            {
                Arrays.fill(progress, 0);
            } else
            {
                for(int i = 0; i < progress.length; i++)
                {
                    if(progress[i] != 0 && progress[i] < requiredFluids.get(i).amount) // Only reset progress for incomplete entries
                    {
                        progress[i] = 0;
                        updated = true;
                    }
                }
            }
        }
		
		for(int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			ItemStack stack = player.inventory.getStackInSlot(i);
			if(stack == null || !(stack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isFilledContainer(stack))) continue;
			
			for(int j = 0; j < requiredFluids.size(); j++)
			{
				FluidStack rStack = requiredFluids.get(j);
				
				if(progress[j] >= rStack.amount || !rStack.isFluidEqual(stack)) continue;
				
				int remaining = rStack.amount - progress[j];
				
				FluidStack fluid = getFluid(player, i, consume, remaining);
				if(fluid != null && fluid.amount > 0)
                {
                    progress[j] += fluid.amount;
                    updated = true;
                }
				
				break;
			}
		}
		
		if(updated) setUserProgress(playerID, progress);
		
		boolean hasAll = true;
		int[] totalProgress = quest == null || !quest.getProperty(NativeProps.GLOBAL) ? getPartyProgress(playerID) : getGlobalProgress();
		
		for(int j = 0; j < requiredFluids.size(); j++)
		{
			FluidStack rStack = requiredFluids.get(j);
			
			if(totalProgress[j] >= rStack.amount) continue;
			
			hasAll = false;
			break;
		}
		
		if(hasAll)
		{
			setComplete(playerID);
			updated = true;
		}
		
		if(updated)
        {
            QuestCache qc = (QuestCache)player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
            if(qc != null) qc.markQuestDirty(QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
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
			return ((IFluidContainerItem)stack.getItem()).drain(stack, amount, drain);
		} else
		{
		    // TODO: Revise this math later
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
	public NBTTagCompound writeToNBT(NBTTagCompound json)
	{
	    //json.setBoolean("partialMatch", partialMatch);
		json.setBoolean("ignoreNBT", ignoreNbt);
		json.setBoolean("consume", consume);
		json.setBoolean("groupDetect", groupDetect);
		json.setBoolean("autoConsume", autoConsume);
		
		NBTTagList itemArray = new NBTTagList();
		for(FluidStack stack : this.requiredFluids)
		{
			itemArray.appendTag(stack.writeToNBT(new NBTTagCompound()));
		}
		json.setTag("requiredFluids", itemArray);
		
		return json;
	}

	@Override
	public void readFromNBT(NBTTagCompound json)
	{
	    //partialMatch = json.getBoolean("partialMatch");
		ignoreNbt = json.getBoolean("ignoreNBT");
		consume = json.getBoolean("consume");
		groupDetect = json.getBoolean("groupDetect");
		autoConsume = json.getBoolean("autoConsume");
		
		requiredFluids.clear();
		NBTTagList fList = json.getTagList("requiredFluids", 10);
		for(int i = 0; i < fList.tagCount(); i++)
		{
			requiredFluids.add(JsonHelper.JsonToFluidStack(fList.getCompoundTagAt(i)));
		}
	}
	
	@Override
	public void readProgressFromNBT(NBTTagCompound json, boolean merge)
	{
		completeUsers.clear();
		NBTTagList cList = json.getTagList("completeUsers", 8);
		for(int i = 0; i < cList.tagCount(); i++)
		{
			try
			{
				completeUsers.add(UUID.fromString(cList.getStringTagAt(i)));
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load UUID for task", e);
			}
		}
		
		userProgress.clear();
		NBTTagList pList = json.getTagList("userProgress", 10);
		for(int n = 0; n < pList.tagCount(); n++)
        {
			NBTTagCompound pTag = pList.getCompoundTagAt(n);
			UUID uuid;
			try
			{
				uuid = UUID.fromString(pTag.getString("uuid"));
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load user progress for task", e);
				continue;
			}
			
			int[] data = new int[requiredFluids.size()];
			List<NBTBase> dJson = NBTConverter.getTagList(pTag.getTagList("data", 3));
			for(int i = 0; i < data.length && i < dJson.size(); i++)
			{
				try
				{
					data[i] = ((NBTPrimitive)dJson.get(i)).func_150287_d();
				} catch(Exception e)
				{
					BQ_Standard.logger.log(Level.ERROR, "Incorrect task progress format", e);
				}
			}
			
			userProgress.put(uuid, data);
		}
	}
	
	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound json, List<UUID> users)
	{
		NBTTagList jArray = new NBTTagList();
		for(UUID uuid : completeUsers)
		{
			jArray.appendTag(new NBTTagString(uuid.toString()));
		}
		json.setTag("completeUsers", jArray);
		
		NBTTagList progArray = new NBTTagList();
		for(Entry<UUID,int[]> entry : userProgress.entrySet())
		{
			NBTTagCompound pJson = new NBTTagCompound();
			pJson.setString("uuid", entry.getKey().toString());
			NBTTagList pArray = new NBTTagList();
			for(int i : entry.getValue())
			{
				pArray.appendTag(new NBTTagInt(i));
			}
			pJson.setTag("data", pArray);
			progArray.appendTag(pJson);
		}
		json.setTag("userProgress", progArray);
		
		return json;
	}

	@Override
	public void resetUser(UUID uuid)
	{
		completeUsers.remove(uuid);
		userProgress.remove(uuid);
	}

	@Override
	public void resetAll()
	{
		completeUsers.clear();
		userProgress.clear();
	}
	
	@Override
	public float getParticipation(UUID uuid)
	{
		if(requiredFluids.size() <= 0)
		{
			return 1F;
		}
		
		float total = 0F;
		
		int[] progress = getUsersProgress(uuid);
		for(int i = 0; i < requiredFluids.size(); i++)
		{
			FluidStack rStack = requiredFluids.get(i);
			total += progress[i] / (float)rStack.amount;
		}
		
		return total / (float)requiredFluids.size();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
	{
	    return new PanelTaskFluid(rect, quest, this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen screen, IQuest quest)
	{
		return null;
	}

	@Override
	public boolean canAcceptFluid(UUID owner, IQuest quest, FluidStack fluid)
	{
		if(owner == null || fluid == null || fluid.getFluid() == null || !consume || isComplete(owner) || requiredFluids.size() <= 0)
		{
			return false;
		}
		
		int[] progress = getUsersProgress(owner);
		
		for(int j = 0; j < requiredFluids.size(); j++)
		{
			FluidStack rStack = requiredFluids.get(j).copy();
			if(ignoreNbt) rStack.tag = null;
			if(progress[j] < rStack.amount && rStack.equals(fluid)) return true;
		}
		
		return false;
	}

	@Override
	public boolean canAcceptItem(UUID owner, IQuest quest, ItemStack item)
	{
		if(owner == null || item == null || !consume || isComplete(owner) || requiredFluids.size() <= 0)
		{
			return false;
		}
		
		if(item.getItem() instanceof IFluidContainerItem)
        {
            return canAcceptFluid(owner, quest, ((IFluidContainerItem)item.getItem()).getFluid(item));
        } else if(FluidContainerRegistry.isFilledContainer(item))
        {
            return canAcceptFluid(owner, quest, FluidContainerRegistry.getFluidForFilledItem(item));
        }
		
		return false;
	}

	@Override
	public FluidStack submitFluid(UUID owner, IQuest quest, FluidStack fluid)
	{
		if(owner == null || fluid == null || fluid.amount <= 0 || !consume || isComplete(owner) || requiredFluids.size() <= 0)
		{
			return fluid;
		}
		
		int[] progress = getUsersProgress(owner);
		
		for(int j = 0; j < requiredFluids.size(); j++)
		{
			FluidStack rStack = requiredFluids.get(j);
			
			if(progress[j] >= rStack.amount) continue;
			
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
			setUserProgress(owner, progress);
		}
		
		return fluid;
	}

	@Override
	public ItemStack submitItem(UUID owner, IQuest quest, ItemStack input)
	{
		if(owner == null || input == null || !consume || isComplete(owner)) return input;
		
		ItemStack item = input.splitStack(1); // Prevents issues with stack filling/draining
        
        if(item.getItem() instanceof IFluidContainerItem)
        {
            IFluidContainerItem container = (IFluidContainerItem)item.getItem();
            FluidStack fluid = container.drain(item, container.getCapacity(item), false);
            int amount = fluid.amount;
            fluid = submitFluid(owner, quest, fluid.copy());
            container.drain(item, fluid == null ? amount : amount - fluid.amount, true);
            return item;
        } else if(FluidContainerRegistry.isFilledContainer(item))
        {
            FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(item);
            submitFluid(owner, quest, fluid);
            return FluidContainerRegistry.drainFluidContainer(item);
        }
		
		return item;
	}

	@Override
	public void setUserProgress(UUID uuid, int[] progress)
	{
		userProgress.put(uuid, progress);
	}

	@Override
	public int[] getUsersProgress(UUID... users)
	{
		int[] progress = new int[requiredFluids.size()];
		
		for(UUID uuid : users)
		{
			int[] tmp = userProgress.get(uuid);
			
			if(tmp == null || tmp.length != requiredFluids.size()) continue;
			
			for(int n = 0; n < progress.length; n++)
			{
			    if(!consume)
                {
                    progress[n] = Math.max(progress[n], tmp[n]);
                } else
                {
				    progress[n] += tmp[n];
                }
			}
		}
		
		return progress;
	}
	
	public int[] getPartyProgress(UUID uuid)
	{
		IParty party = QuestingAPI.getAPI(ApiReference.PARTY_DB).getUserParty(uuid);
        return getUsersProgress(party == null ? new UUID[]{uuid} : party.getMembers().toArray(new UUID[0]));
	}

	@Override
	public int[] getGlobalProgress()
	{
		int[] total = new int[requiredFluids.size()];
		
		for(int[] up : userProgress.values())
		{
			if(up == null || up.length != requiredFluids.size()) continue;
			
			for(int i = 0; i < up.length; i++)
			{
				if(!consume)
				{
					total[i] = Math.max(total[i], up[i]);
				} else
				{
					total[i] += up[i];
				}
			}
		}
		
		return total;
	}
}
