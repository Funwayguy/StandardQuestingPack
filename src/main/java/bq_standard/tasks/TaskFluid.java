package bq_standard.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.IFluidTask;
import betterquesting.api.questing.tasks.IItemTask;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.api2.utils.Tuple2;
import bq_standard.client.gui.tasks.PanelTaskFluid;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskFluid;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TaskFluid implements ITaskInventory, IFluidTask, IItemTask
{
	private final Set<UUID> completeUsers = new TreeSet<>();
	public final List<FluidStack> requiredFluids = new ArrayList<>();
	public final TreeMap<UUID, int[]> userProgress = new TreeMap<>();
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
		completeUsers.add(uuid);
	}
	
	@Override
	public void onInventoryChange(@Nonnull DBEntry<IQuest> quest, @Nonnull ParticipantInfo pInfo)
	{
        if(!consume || autoConsume)
        {
            detect(pInfo, quest);
        }
	}

	@Override
	public void detect(ParticipantInfo pInfo, DBEntry<IQuest> quest)
	{
	    if(isComplete(pInfo.UUID)) return;
	    
	    // Removing the consume check here would make the task cheaper on groups and for that reason sharing is restricted to detect only
        final List<Tuple2<UUID, int[]>> progress = getBulkProgress(consume ? Collections.singletonList(pInfo.UUID) : pInfo.ALL_UUIDS);
		boolean updated = false;
		
        if(!consume)
        {
            if(groupDetect) // Reset all detect progress
            {
                progress.forEach((value) -> Arrays.fill(value.getSecond(), 0));
            } else
            {
                for(int i = 0; i < requiredFluids.size(); i++)
                {
                    final int r = requiredFluids.get(i).amount;
                    for(Tuple2<UUID, int[]> value : progress)
                    {
                        int n = value.getSecond()[i];
                        if(n != 0 && n < r)
                        {
                            value.getSecond()[i] = 0;
                            updated = true;
                        }
                    }
                }
            }
        }
		
		final List<InventoryPlayer> invoList;
		if(consume)
        {
            // We do not support consuming resources from other member's invetories.
            // This could otherwise be abused to siphon items/fluids unknowingly
            invoList = Collections.singletonList(pInfo.PLAYER.inventory);
        } else
        {
            invoList = new ArrayList<>();
            pInfo.ACTIVE_PLAYERS.forEach((p) -> invoList.add(p.inventory));
        }
		
		for(InventoryPlayer invo : invoList)
        {
            for(int i = 0; i < invo.getSizeInventory(); i++)
            {
                ItemStack stack = invo.getStackInSlot(i);
                if(stack == null || stack.stackSize <= 0) continue;
                if(!(stack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isFilledContainer(stack))) continue;
                
                for(int j = 0; j < requiredFluids.size(); j++)
                {
                    final FluidStack rStack = requiredFluids.get(j);
                    FluidStack drainOG = rStack.copy();
                    if(ignoreNbt) drainOG.tag = null;
                    
                    // Pre-check
                    FluidStack sample = getFluid(invo, i, false, drainOG.amount);
                    if(!drainOG.isFluidEqual(sample)) continue;
                    
                    for(Tuple2<UUID, int[]> value : progress)
                    {
                        if(value.getSecond()[j] >= rStack.amount) continue;
                        int remaining = rStack.amount - value.getSecond()[j];
                        
                        FluidStack drain = rStack.copy();
                        drain.amount = remaining; //drain.amount = remaining / stack.stackSize;
                        if(ignoreNbt) drain.tag = null;
                        if(drain.amount <= 0) continue;
                        
                        FluidStack fluid = getFluid(invo, i, consume, drain.amount);
                        if(fluid == null || fluid.amount <= 0) continue;
            
                        value.getSecond()[j] += fluid.amount * stack.stackSize;
                        updated = true;
                    }
                }
            }
        }
		
		if(updated) setBulkProgress(progress);
		checkAndComplete(pInfo, quest, updated);
	}
	
	private void checkAndComplete(ParticipantInfo pInfo, DBEntry<IQuest> quest, boolean resync)
    {
        final List<Tuple2<UUID, int[]>> progress = getBulkProgress(consume ? Collections.singletonList(pInfo.UUID) : pInfo.ALL_UUIDS);
        boolean updated = resync;
        
        topLoop:
        for(Tuple2<UUID, int[]> value : progress)
        {
            for(int j = 0; j < requiredFluids.size(); j++)
            {
                if(value.getSecond()[j] >= requiredFluids.get(j).amount) continue;
                continue topLoop;
            }
            
            updated = true;
            
            if(consume)
            {
                setComplete(value.getFirst());
            } else
            {
                progress.forEach((pair) -> setComplete(pair.getFirst()));
                break;
            }
        }
		
		if(updated)
        {
            if(consume)
            {
                pInfo.markDirty(Collections.singletonList(quest.getID()));
            } else
            {
                pInfo.markDirtyParty(Collections.singletonList(quest.getID()));
            }
        }
    }
	
	/**
	 * Returns the fluid drained (or can be drained) up to the specified amount
	 */
	private FluidStack getFluid(InventoryPlayer invo, int slot, boolean drain, int amount)
	{
		ItemStack stack = invo.getStackInSlot(slot);
		
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
					invo.decrStackSize(slot, 1);
					
					if(!invo.addItemStackToInventory(empty))
					{
						invo.player.dropPlayerItemWithRandomChoice(empty, false);
					}
				}
			}
			
			return fluid;
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
	    //json.setBoolean("partialMatch", partialMatch);
		nbt.setBoolean("ignoreNBT", ignoreNbt);
		nbt.setBoolean("consume", consume);
		nbt.setBoolean("groupDetect", groupDetect);
		nbt.setBoolean("autoConsume", autoConsume);
		
		NBTTagList itemArray = new NBTTagList();
		for(FluidStack stack : this.requiredFluids)
		{
			itemArray.appendTag(stack.writeToNBT(new NBTTagCompound()));
		}
		nbt.setTag("requiredFluids", itemArray);
		
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
	    //partialMatch = json.getBoolean("partialMatch");
		ignoreNbt = nbt.getBoolean("ignoreNBT");
		consume = nbt.getBoolean("consume");
		groupDetect = nbt.getBoolean("groupDetect");
		autoConsume = nbt.getBoolean("autoConsume");
		
		requiredFluids.clear();
		NBTTagList fList = nbt.getTagList("requiredFluids", 10);
		for(int i = 0; i < fList.tagCount(); i++)
		{
			requiredFluids.add(JsonHelper.JsonToFluidStack(fList.getCompoundTagAt(i)));
		}
	}
	
	@Override
	public void readProgressFromNBT(NBTTagCompound nbt, boolean merge)
	{
		if(!merge)
        {
            completeUsers.clear();
            userProgress.clear();
        }
		
		NBTTagList cList = nbt.getTagList("completeUsers", 8);
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
		
		NBTTagList pList = nbt.getTagList("userProgress", 10);
		for(int n = 0; n < pList.tagCount(); n++)
		{
			try
			{
                NBTTagCompound pTag = pList.getCompoundTagAt(n);
                UUID uuid = UUID.fromString(pTag.getString("uuid"));
                
                int[] data = new int[requiredFluids.size()];
			    List<NBTBase> dNbt = NBTConverter.getTagList(pTag.getTagList("data", 3));
                for(int i = 0; i < data.length && i < dNbt.size(); i++) // TODO: Change this to an int array. This is dumb...
                {
					data[i] = ((NBTPrimitive)dNbt.get(i)).func_150287_d();
                }
                
			    userProgress.put(uuid, data);
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load user progress for task", e);
			}
		}
	}
	
	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, @Nullable List<UUID> users)
	{
		NBTTagList jArray = new NBTTagList();
		NBTTagList progArray = new NBTTagList();
		
		if(users != null)
        {
            users.forEach((uuid) -> {
                if(completeUsers.contains(uuid)) jArray.appendTag(new NBTTagString(uuid.toString()));
                
                int[] data = userProgress.get(uuid);
                if(data != null)
                {
                    NBTTagCompound pJson = new NBTTagCompound();
                    pJson.setString("uuid", uuid.toString());
                    NBTTagList pArray = new NBTTagList(); // TODO: Why the heck isn't this just an int array?!
                    for(int i : data) pArray.appendTag(new NBTTagInt(i));
                    pJson.setTag("data", pArray);
                    progArray.appendTag(pJson);
                }
            });
        } else
        {
            completeUsers.forEach((uuid) -> jArray.appendTag(new NBTTagString(uuid.toString())));
            
            userProgress.forEach((uuid, data) -> {
                NBTTagCompound pJson = new NBTTagCompound();
			    pJson.setString("uuid", uuid.toString());
                NBTTagList pArray = new NBTTagList(); // TODO: Why the heck isn't this just an int array?!
                for(int i : data) pArray.appendTag(new NBTTagInt(i));
                pJson.setTag("data", pArray);
                progArray.appendTag(pJson);
            });
        }
		
		nbt.setTag("completeUsers", jArray);
		nbt.setTag("userProgress", progArray);
		
		return nbt;
	}

	@Override
	public void resetUser(@Nullable UUID uuid)
	{
	    if(uuid == null)
        {
            completeUsers.clear();
            userProgress.clear();
        } else
        {
            completeUsers.remove(uuid);
            userProgress.remove(uuid);
        }
	}
 
	@Override
	@SideOnly(Side.CLIENT)
	public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest)
	{
	    return new PanelTaskFluid(rect, this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen screen, DBEntry<IQuest> quest)
	{
		return null;
	}

	@Override
	public boolean canAcceptFluid(UUID owner, DBEntry<IQuest> quest, FluidStack fluid)
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
	public boolean canAcceptItem(UUID owner, DBEntry<IQuest> quest, ItemStack item)
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
	public FluidStack submitFluid(UUID owner, DBEntry<IQuest> quest, FluidStack fluid)
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
	public ItemStack submitItem(UUID owner, DBEntry<IQuest> quest, ItemStack input)
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
 
	private void setUserProgress(UUID uuid, int[] progress)
	{
		userProgress.put(uuid, progress);
	}
 
	public int[] getUsersProgress(UUID uuid)
	{
		int[] progress = userProgress.get(uuid);
		return progress == null || progress.length != requiredFluids.size()? new int[requiredFluids.size()] : progress;
	}
	
	private List<Tuple2<UUID, int[]>> getBulkProgress(@Nonnull List<UUID> uuids)
    {
        if(uuids.size() <= 0) return Collections.emptyList();
        List<Tuple2<UUID, int[]>> list = new ArrayList<>();
        uuids.forEach((key) -> list.add(new Tuple2<>(key, getUsersProgress(key))));
        return list;
    }
    
    private void setBulkProgress(@Nonnull List<Tuple2<UUID, int[]>> list)
    {
        list.forEach((entry) -> setUserProgress(entry.getFirst(), entry.getSecond()));
    }
}
