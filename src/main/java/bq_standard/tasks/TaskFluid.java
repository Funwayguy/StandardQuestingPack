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
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import bq_standard.client.gui.tasks.PanelTaskFluid;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskFluid;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.Map.Entry;

public class TaskFluid implements ITaskInventory, IFluidTask, IItemTask, IProgression<int[]>
{
	private final List<UUID> completeUsers = new ArrayList<>();
	public final NonNullList<FluidStack> requiredFluids = NonNullList.create();
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
			if(stack.isEmpty()) continue;
			IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
			if(handler == null) continue;
			
			boolean hasDrained = false;
			
			for(int j = 0; j < requiredFluids.size(); j++)
			{
				FluidStack rStack = requiredFluids.get(j);
				
				if(progress[j] >= rStack.amount) continue;
				
				int remaining = rStack.amount - progress[j];
				
				FluidStack drain = rStack.copy();
				drain.amount = remaining / stack.getCount(); // Must be a multiple of the stack size
				if(ignoreNbt) drain.tag = null;
				
				if(drain.amount <= 0) continue;
				
				FluidStack fluid = handler.drain(drain, consume);
				if(fluid == null || fluid.amount <= 0) continue;
				
				progress[j] += fluid.amount * stack.getCount();
                hasDrained = true;
				updated = true;
			}
			
			if(hasDrained && consume) player.inventory.setInventorySlotContents(i, handler.getContainer());
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
            QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
            if(qc != null) qc.markQuestDirty(QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
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
			NBTTagList dJson = pTag.getTagList("data", 3);
			for(int i = 0; i < data.length && i < dJson.tagCount(); i++)
			{
				try
				{
					data[i] = dJson.getIntAt(i);
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
		if(owner == null || item == null || item.isEmpty() || !consume || isComplete(owner) || requiredFluids.size() <= 0)
		{
			return false;
		}
		
		IFluidHandlerItem handler = FluidUtil.getFluidHandler(item);
		
		if(handler == null) return false;
		
		for(IFluidTankProperties tank : handler.getTankProperties())
        {
            if(!tank.canDrain()) continue;
            
            for(FluidStack rStack : requiredFluids)
            {
                if(rStack.equals(tank.getContents())) return true;
            }
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
		if(owner == null || input.isEmpty() || !consume || isComplete(owner)) return input;
		
		ItemStack item = input.splitStack(1); // Prevents issues with stack filling/draining

		IFluidHandlerItem handler = FluidUtil.getFluidHandler(item);
		if(handler == null) return item;
		
        boolean hasDrained = false;
        
        for(IFluidTankProperties tank : handler.getTankProperties())
        {
            if(!tank.canDrain() || tank.getContents() == null || !tank.canDrainFluidType(tank.getContents())) continue;
            
            FluidStack remaining = submitFluid(owner, quest, tank.getContents());
            FluidStack drain = tank.getContents().copy();
            drain.amount -= remaining == null ? 0 : remaining.amount;
            
            if(drain.amount <= 0) continue;
            
            handler.drain(tank.getContents(), true);
            hasDrained = true;
        }
		
		return hasDrained ? handler.getContainer() : item;
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
