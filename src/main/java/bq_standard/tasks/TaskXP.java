package bq_standard.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import bq_standard.XPHelper;
import bq_standard.client.gui.tasks.PanelTaskXP;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskXP;
import net.minecraft.client.gui.GuiScreen;
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
	private final TreeMap<UUID, Long> userProgress = new TreeMap<>();
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
	public void tickTask(@Nonnull ParticipantInfo pInfo, DBEntry<IQuest> quest)
	{
	    if(consume || pInfo.PLAYER.ticksExisted%60 != 0) return; // Every 3 seconds
        
        long curProg = getUsersProgress(pInfo.UUID);
        long nxtProg = XPHelper.getPlayerXP(pInfo.PLAYER);
        
        if(curProg != nxtProg)
        {
            setUserProgress(pInfo.UUID, XPHelper.getPlayerXP(pInfo.PLAYER));
            pInfo.markDirty(Collections.singletonList(quest.getID()));
        }
        
        long rawXP = levels? XPHelper.getLevelXP(amount) : amount;
        long totalXP = getUsersProgress(pInfo.UUID);
        
        if(totalXP >= rawXP) setComplete(pInfo.UUID);
	}
	
	@Override
	public void detect(ParticipantInfo pInfo, DBEntry<IQuest> quest)
	{
		if(isComplete(pInfo.UUID)) return;
		
		long progress = getUsersProgress(pInfo.UUID);
		long rawXP = levels? XPHelper.getLevelXP(amount) : amount;
		long plrXP = XPHelper.getPlayerXP(pInfo.PLAYER);
		long remaining = rawXP - progress;
		long cost = Math.min(remaining, plrXP);
		
		boolean changed = false;
		
		if(consume && cost != 0)
        {
            progress += cost;
            setUserProgress(pInfo.UUID, progress);
            XPHelper.addXP(pInfo.PLAYER, -cost);
            changed = true;
		} else if(!consume && progress != plrXP)
        {
            setUserProgress(pInfo.UUID, plrXP);
            changed = true;
        }
		
		long totalXP = getUsersProgress(pInfo.UUID);
		
		if(totalXP >= rawXP)
        {
            setComplete(pInfo.UUID);
            changed = true;
        }
		
		if(changed) // Needs to be here because even if no additional progress was added, a party memeber may have completed the task anyway
        {
            pInfo.markDirty(Collections.singletonList(quest.getID()));
        }
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.xp";
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("amount", amount);
		nbt.setBoolean("isLevels", levels);
		nbt.setBoolean("consume", consume);
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		amount = nbt.hasKey("amount", 99) ? nbt.getInteger("amount") : 30;
		levels = nbt.getBoolean("isLevels");
		consume = nbt.getBoolean("consume");
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
	public IGuiPanel getTaskGui(IGuiRect rect, DBEntry<IQuest> quest)
	{
	    return new PanelTaskXP(rect, this);
	}
	
	@Override
	public GuiScreen getTaskEditor(GuiScreen screen, DBEntry<IQuest> quest)
	{
		return null;
	}
	
	private void setUserProgress(UUID uuid, long progress)
	{
		userProgress.put(uuid, progress);
	}
	
	public long getUsersProgress(UUID uuid)
	{
        Long n = userProgress.get(uuid);
        return n == null? 0 : n;
	}
}
