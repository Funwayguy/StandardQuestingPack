package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.IItemTask;
import betterquesting.api.questing.tasks.IProgression;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import bq_standard.client.gui2.tasks.PanelTaskRetrieval;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskRetrieval;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class TaskRetrieval implements ITask, IProgression<int[]>, IItemTask, ITaskTickable
{
	private final List<UUID> completeUsers = new ArrayList<>();
	public final NonNullList<BigItemStack> requiredItems = NonNullList.create();
	private final HashMap<UUID, int[]> userProgress = new HashMap<>();
	public boolean partialMatch = true;
	public boolean ignoreNBT = false;
	public boolean consume = true;
	public boolean idvDetect = true;
	public boolean autoConsume = false;
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.retrieval";
	}
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskRetrieval.INSTANCE.getRegistryName();
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
	public void tickTask(IQuest quest, EntityPlayer player)
	{
		if(player.ticksExisted%60 == 0)
		{
			if(!consume || autoConsume)
			{
				detect(player, quest);
			} else
			{
				boolean flag = true;
				
				int[] totalProgress = quest == null || !quest.getProperty(NativeProps.GLOBAL)? getPartyProgress(QuestingAPI.getQuestingUUID(player)) : getGlobalProgress();
				for(int j = 0; j < requiredItems.size(); j++)
				{
					BigItemStack rStack = requiredItems.get(j);
					
					if(totalProgress[j] >= rStack.stackSize) continue;
					
					flag = false;
					break;
				}
				
				if(flag)
				{
					setComplete(QuestingAPI.getQuestingUUID(player));
				}
			}
		}
	}

	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		if(player.inventory == null || isComplete(playerID)) return;
		
		int[] progress = this.getUsersProgress(playerID);
		boolean updated = false;
		
		for(int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			for(int j = 0; j < requiredItems.size(); j++)
			{
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(stack.isEmpty())
				{
					break;
				}
				
				BigItemStack rStack = requiredItems.get(j);
				
				if(progress[j] >= rStack.stackSize) continue;
				
				int remaining = rStack.stackSize - progress[j];
				if(remaining <= 0) continue;
				
				if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch) || ItemComparison.OreDictionaryMatch(rStack.oreDict, rStack.GetTagCompound(), stack, !ignoreNBT, partialMatch))
				{
					if(consume)
					{
						ItemStack removed = player.inventory.decrStackSize(i, remaining);
						progress[j] += removed.getCount();
					} else
					{
						progress[j] += Math.min(remaining, stack.getCount());
					}
					
					updated = true;
				}
			}
		}
		
		if(!consume && idvDetect) // Resets incomplete detections
		{
			for(int i = 0; i < progress.length; i++)
			{
				if(progress[i] != 0 && progress[i] < requiredItems.get(i).stackSize)
				{
					progress[i] = 0;
					updated = true;
				}
			}
		}
		
		boolean flag = true;
		int[] totalProgress = progress;
		
		if(consume || idvDetect)
		{
		    if(updated)
            {
                setUserProgress(playerID, progress);
                QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
                if(qc != null) qc.markQuestDirty(QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
                
            }
			totalProgress = quest == null || !quest.getProperty(NativeProps.GLOBAL)? getPartyProgress(playerID) : getGlobalProgress();
		}
		
		for(int j = 0; j < requiredItems.size(); j++)
		{
			BigItemStack rStack = requiredItems.get(j);
			
			if(totalProgress[j] >= rStack.stackSize) continue;
			
			flag = false;
			break;
		}
		
		if(flag)
		{
			setComplete(playerID);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json)
	{
		json.setBoolean("partialMatch", partialMatch);
		json.setBoolean("ignoreNBT", ignoreNBT);
		json.setBoolean("consume", consume);
		json.setBoolean("groupDetect", !idvDetect);
		json.setBoolean("autoConsume", autoConsume);
		
		NBTTagList itemArray = new NBTTagList();
		for(BigItemStack stack : this.requiredItems)
		{
			itemArray.appendTag(JsonHelper.ItemStackToJson(stack, new NBTTagCompound()));
		}
		json.setTag("requiredItems", itemArray);
		
		return json;
	}

	@Override
	public void readFromNBT(NBTTagCompound json)
	{
		partialMatch = json.getBoolean("partialMatch");
		ignoreNBT = json.getBoolean("ignoreNBT");
		consume = json.getBoolean("consume");
		idvDetect = !json.getBoolean("groupDetect");
		autoConsume = json.getBoolean("autoConsume");
		
		requiredItems.clear();
		NBTTagList iList = json.getTagList("requiredItems", 10);
		for(int i = 0; i < iList.tagCount(); i++)
		{
			requiredItems.add(JsonHelper.JsonToItemStack(iList.getCompoundTagAt(i)));
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
			
			int[] data = new int[requiredItems.size()];
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
		if(requiredItems.size() <= 0)
		{
			return 1F;
		}
		
		float total = 0F;
		
		int[] progress = getUsersProgress(uuid);
		for(int i = 0; i < requiredItems.size(); i++)
		{
			BigItemStack rStack = requiredItems.get(i);
			total += progress[i] / (float)rStack.stackSize;
		}
		
		return total / (float)requiredItems.size();
	}

	@Override
	public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
	{
	    return new PanelTaskRetrieval(rect, quest, this);
	}
	
	@Override
	public boolean canAcceptItem(UUID owner, ItemStack stack)
	{
		if(owner == null || stack == null || stack.isEmpty() || !consume || isComplete(owner) || requiredItems.size() <= 0)
		{
			return false;
		}
		
		int[] progress = getUsersProgress(owner);
		
		for(int j = 0; j < requiredItems.size(); j++)
		{
			BigItemStack rStack = requiredItems.get(j);
			
			if(progress[j] >= rStack.stackSize) continue;
			
			if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch) || ItemComparison.OreDictionaryMatch(rStack.oreDict, rStack.GetTagCompound(), stack, !ignoreNBT, partialMatch))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public ItemStack submitItem(UUID owner, ItemStack input)
	{
		ItemStack stack = input.copy();
		
		if(owner == null || stack.isEmpty() || !consume || isComplete(owner)) return stack;
		
		int[] progress = getUsersProgress(owner);
		boolean updated = false;
		
		for(int j = 0; j < requiredItems.size(); j++)
		{
			if(stack.isEmpty()) break;
			
			BigItemStack rStack = requiredItems.get(j);
			
			if(progress[j] >= rStack.stackSize) continue;

			int remaining = rStack.stackSize - progress[j];
			
			if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch) || ItemComparison.OreDictionaryMatch(rStack.oreDict, rStack.GetTagCompound(), stack, !ignoreNBT, partialMatch))
			{
				int removed = Math.min(stack.getCount(), remaining);
				stack.shrink(removed);
				progress[j] += removed;
				updated = true;
				if(stack.isEmpty()) break;
			}
		}
		
		if(updated)
        {
            setUserProgress(owner, progress);
        }
		
		return stack.isEmpty() ? ItemStack.EMPTY : stack;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)//, JsonObject data)
	{
		return null;//new GuiRetrievalEditor(parent, this, quest);
	}

	@Override
	public void setUserProgress(UUID uuid, int[] progress)
	{
		userProgress.put(uuid, progress);
	}
	
	@Override
	public int[] getUsersProgress(UUID... users)
	{
		int[] progress = new int[requiredItems.size()];
		
		for(UUID uuid : users)
		{
			int[] tmp = userProgress.get(uuid);
			
			if(tmp == null || tmp.length != requiredItems.size())
			{
				continue;
			}
			
			for(int n = 0; n < progress.length; n++)
			{
				progress[n] += tmp[n];
			}
		}
		
		return progress.length != requiredItems.size()? new int[requiredItems.size()] : progress;
	}
	
	public int[] getPartyProgress(UUID uuid)
	{
		int[] total = new int[requiredItems.size()];
		
		IParty party = QuestingAPI.getAPI(ApiReference.PARTY_DB).getUserParty(uuid);
		
		if(party == null)
		{
			return getUsersProgress(uuid);
		} else
		{
			for(UUID mem : party.getMembers())
			{
				if(mem != null && party.getStatus(mem).ordinal() <= 0)
				{
					continue;
				}

				int[] progress = getUsersProgress(mem);
				
				for(int i = 0; i < progress.length; i++)
				{
					if(idvDetect)
					{
						total[i] = Math.max(total[i], progress[i]);
					} else
					{
						total[i] += progress[i];
					}
				}
			}
		}
		
		return total;
	}

	@Override
	public int[] getGlobalProgress()
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
				if(idvDetect)
				{
					total[i] = Math.max(total[i], progress[i]);
				} else
				{
					total[i] += progress[i];
				}
			}
		}
		
		return total;
	}

	@Override
	public IJsonDoc getDocumentation()
	{
		return null;
	}
}
