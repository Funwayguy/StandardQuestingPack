package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import bq_standard.XPHelper;
import bq_standard.client.gui.tasks.PanelTaskXP;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskXP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class TaskXP implements ITaskTickable
{
	private final List<UUID> completeUsers = new ArrayList<>();
	private final HashMap<UUID, Long> userProgress = new HashMap<>();
	public boolean levels = true;
	public int amount = 30;
	public boolean consume = true;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskXP.INSTANCE.getRegistryName();
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
	public void tickTask(@Nonnull DBEntry<IQuest> quest, @Nonnull EntityPlayer player)
	{
	    if(consume || player.ticksExisted%60 != 0) return; // Every 3 seconds
	    
		UUID playerID = QuestingAPI.getQuestingUUID(player);
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        
        long curProg = getUsersProgress(playerID);
        long nxtProg = XPHelper.getPlayerXP(player);
        
        if(curProg != nxtProg)
        {
            setUserProgress(playerID, XPHelper.getPlayerXP(player));
            if(qc != null) qc.markQuestDirty(quest.getID());
        }
        
        long rawXP = levels? XPHelper.getLevelXP(amount) : amount;
        long totalXP = getUsersProgress(playerID);
        
        if(totalXP >= rawXP) setComplete(playerID);
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		if(isComplete(playerID)) return;
		
		long progress = getUsersProgress(playerID);
		long rawXP = levels? XPHelper.getLevelXP(amount) : amount;
		long plrXP = XPHelper.getPlayerXP(player);
		long remaining = rawXP - progress;
		long cost = Math.min(remaining, plrXP);
		
		boolean changed = false;
		
		if(consume && cost != 0)
        {
            progress += cost;
            setUserProgress(playerID, progress);
            XPHelper.addXP(player, -cost);
            changed = true;
		} else if(!consume && progress != plrXP)
        {
            setUserProgress(playerID, plrXP);
            changed = true;
        }
		
		long totalXP = getUsersProgress(playerID);
		
		if(totalXP >= rawXP)
        {
            setComplete(playerID);
            changed = true;
        }
		
		if(changed) // Needs to be here because even if no additional progress was added, a party memeber may have completed the task anyway
        {
            QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
            if(qc != null) qc.markQuestDirty(QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
        }
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.xp";
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json)
	{
		json.setInteger("amount", amount);
		json.setBoolean("isLevels", levels);
		json.setBoolean("consume", consume);
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json)
	{
		amount = json.hasKey("amount", 99) ? json.getInteger("amount") : 30;
		levels = json.getBoolean("isLevels");
		consume = json.getBoolean("consume");
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
		for(int i = 0; i < pList.tagCount(); i++)
		{
			NBTTagCompound pTag = pList.getCompoundTagAt(i);
			
			UUID uuid;
			try
			{
				uuid = UUID.fromString(pTag.getString("uuid"));
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load user progress for task", e);
				continue;
			}
			
			userProgress.put(uuid, pTag.getLong("value"));
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
		for(Entry<UUID,Long> entry : userProgress.entrySet())
		{
			NBTTagCompound pJson = new NBTTagCompound();
			pJson.setString("uuid", entry.getKey().toString());
			pJson.setLong("value", entry.getValue());
			progArray.appendTag(pJson);
		}
		json.setTag("userProgress", progArray);
		
		return json;
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
	public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
	{
	    return new PanelTaskXP(rect, this);
	}
	
	@Override
	public GuiScreen getTaskEditor(GuiScreen screen, IQuest quest)
	{
		return null;
	}
	
	public void setUserProgress(UUID uuid, long progress)
	{
		userProgress.put(uuid, progress);
	}
	
	public long getUsersProgress(UUID uuid)
	{
        Long n = userProgress.get(uuid);
        return n == null? 0 : n;
	}
}
