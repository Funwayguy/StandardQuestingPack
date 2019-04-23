package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.IProgression;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import bq_standard.client.gui.editors.tasks.GuiEditTaskTame;
import bq_standard.client.gui.tasks.PanelTaskTame;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskTame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class TaskTame implements ITask, IProgression<Integer>
{
	private final List<UUID> completeUsers = new ArrayList<>();
	public final HashMap<UUID, Integer> userProgress = new HashMap<>();
	public String idName = "minecraft:zombie";
	public int required = 1;
	public boolean ignoreNBT = true;
	public boolean subtypes = true;
	
	/**
	 * NBT representation of the intended target. Used only for NBT comparison checks
	 */
	public NBTTagCompound targetTags = new NBTTagCompound();
	
    @Override
    public String getUnlocalisedName()
    {
        return "bq_standard.task.tame";
    }
    
    @Override
    public ResourceLocation getFactoryID()
    {
        return FactoryTaskTame.INSTANCE.getRegistryName();
    }
    
    @Override
    public void detect(EntityPlayer player, IQuest quest)
    {
	    UUID playerID = QuestingAPI.getQuestingUUID(player);
	    
		if(isComplete(playerID)) return;
		
		int progress = quest == null || !quest.getProperty(NativeProps.GLOBAL) ? getPartyProgress(playerID) : getGlobalProgress();
		
		if(progress >= required) setComplete(playerID);
    }
	
	public void onAnimalTamed(DBEntry<IQuest> quest, EntityPlayer player, EntityLivingBase entity)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
		
		if(entity == null || this.isComplete(playerID)) return;
		
		int progress = getUsersProgress(playerID);
		
		Class<? extends Entity> subject = entity.getClass();
		ResourceLocation targetID = new ResourceLocation(idName);
		Class<? extends Entity> target = EntityList.getClass(targetID);
		ResourceLocation subjectID = EntityList.getKey(subject);
		
		if(subjectID == null || target == null)
		{
			return; // Missing necessary data
		} else if(subtypes && !target.isAssignableFrom(subject))
		{
			return; // This is not the intended target or sub-type
		} else if(!subtypes && !subjectID.equals(targetID))
		{
			return; // This isn't the exact target required
		}
		
		NBTTagCompound subjectTags = new NBTTagCompound();
		entity.writeToNBTOptional(subjectTags);
		if(!ignoreNBT && !ItemComparison.CompareNBTTag(targetTags, subjectTags, true))
		{
			return;
		}
		
		setUserProgress(playerID, progress + 1);
		if(qc != null) qc.markQuestDirty(quest.getID());
		
		detect(player, quest.getValue());
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
 
	@Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
    {
        return new PanelTaskTame(rect, quest, this);
    }
    
    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
    {
        return new GuiEditTaskTame(parent, quest, this);
    }
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json)
	{
		json.setString("target", idName);
		json.setInteger("required", required);
		json.setBoolean("subtypes", subtypes);
		json.setBoolean("ignoreNBT", ignoreNBT);
		json.setTag("targetNBT", targetTags);
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json)
	{
		idName = json.getString("target");
		required = json.getInteger("required");
		subtypes = json.getBoolean("subtypes");
		ignoreNBT = json.getBoolean("ignoreNBT");
		targetTags = json.getCompoundTag("targetNBT");
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
			
			userProgress.put(uuid, pTag.getInteger("value"));
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
		for(Entry<UUID,Integer> entry : userProgress.entrySet())
		{
			NBTTagCompound pJson = new NBTTagCompound();
			pJson.setString("uuid", entry.getKey().toString());
			pJson.setInteger("value", entry.getValue());
			progArray.appendTag(pJson);
		}
		json.setTag("userProgress", progArray);
		
		return json;
	}
	
	@Override
	public void setUserProgress(UUID uuid, Integer progress)
	{
		userProgress.put(uuid, progress);
	}
	
	@Override
	public Integer getUsersProgress(UUID... users)
	{
		int i = 0;
		
		for(UUID uuid : users)
		{
			Integer n = userProgress.get(uuid);
			i += n == null? 0 : n;
		}
		
		return i;
	}
	
	public Integer getPartyProgress(UUID uuid)
	{
		IParty party = QuestingAPI.getAPI(ApiReference.PARTY_DB).getUserParty(uuid);
        return getUsersProgress(party == null ? new UUID[]{uuid} : party.getMembers().toArray(new UUID[0]));
	}
	
	@Override
	public Integer getGlobalProgress()
	{
		int total = 0;
		
		for(Integer i : userProgress.values())
		{
			total += i == null? 0 : i;
		}
		
		return total;
	}
	
	@Override
	public float getParticipation(UUID uuid)
	{
		if(required <= 0)
		{
			return 1F;
		}
		
		return getUsersProgress(uuid) / (float)required;
	}
}
