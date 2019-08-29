package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
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
import java.util.*;

public class TaskXP implements ITaskTickable
{
	private final Set<UUID> completeUsers = new TreeSet<>();
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
		completeUsers.add(uuid);
	}
	
	@Override
	public void tickTask(@Nonnull EntityPlayer player, Runnable callback)
	{
	    if(consume || player.ticksExisted%60 != 0) return; // Every 3 seconds
	    
		UUID playerID = QuestingAPI.getQuestingUUID(player);
        
        long curProg = getUsersProgress(playerID);
        long nxtProg = XPHelper.getPlayerXP(player);
        
        if(curProg != nxtProg)
        {
            setUserProgress(playerID, XPHelper.getPlayerXP(player));
            if(callback != null) callback.run();
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
                userProgress.put(uuid, pTag.getLong("value"));
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
                
                Long data = userProgress.get(uuid);
                if(data != null)
                {
                    NBTTagCompound pJson = new NBTTagCompound();
                    pJson.setString("uuid", uuid.toString());
                    pJson.setLong("value", data);
                    progArray.appendTag(pJson);
                }
            });
        } else
        {
            completeUsers.forEach((uuid) -> jArray.appendTag(new NBTTagString(uuid.toString())));
            
            userProgress.forEach((uuid, data) -> {
                NBTTagCompound pJson = new NBTTagCompound();
			    pJson.setString("uuid", uuid.toString());
                pJson.setLong("value", data);
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
