package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.IProgression;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import bq_standard.client.gui2.editors.tasks.GuiEditTaskHunt;
import bq_standard.client.gui2.tasks.PanelTaskHunt;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskHunt;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class TaskHunt implements ITask, IProgression<Integer>
{
	private ArrayList<UUID> completeUsers = new ArrayList<UUID>();
	public HashMap<UUID, Integer> userProgress = new HashMap<UUID, Integer>();
	public String idName = "minecraft:zombie";
	public int required = 1;
	public boolean ignoreNBT = true;
	public boolean subtypes = true;
	
	/**
	 * NBT representation of the intended target. Used only for NBT comparison checks
	 */
	public NBTTagCompound targetTags = new NBTTagCompound();
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskHunt.INSTANCE.getRegistryName();
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
	public String getUnlocalisedName()
	{
		return "bq_standard.task.hunt";
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		if(isComplete(QuestingAPI.getQuestingUUID(player)))
		{
			return;
		}
		
		int progress = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? getPartyProgress(QuestingAPI.getQuestingUUID(player)) : getGlobalProgress();
		
		if(progress >= required)
		{
			setComplete(QuestingAPI.getQuestingUUID(player));
		}
	}
	
	public void onKilledByPlayer(IQuest quest, EntityLivingBase entity, DamageSource source)
	{
		EntityPlayer player = (EntityPlayer)source.getTrueSource();
		
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		if(player == null || entity == null || this.isComplete(playerID))
		{
			return;
		}
		
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
		
		detect(player, quest);
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
		completeUsers = new ArrayList<>();
		NBTTagList cList = json.getTagList("completeUsers", 8);
		for(int i = 0; i < cList.tagCount(); i++)
		{
			NBTBase entry = cList.get(i);
			
			if(entry == null || entry.getId() != 8)
			{
				continue;
			}
			
			try
			{
				completeUsers.add(UUID.fromString(((NBTTagString)entry).getString()));
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load UUID for task", e);
			}
		}
		
		userProgress = new HashMap<>();
		NBTTagList pList = json.getTagList("userProgress", 10);
		for(int i = 0; i < pList.tagCount(); i++)
		{
			NBTBase entry = pList.get(i);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound pTag = (NBTTagCompound)entry;
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
	
	/**
	 * Returns a new editor screen for this Reward type to edit the given data
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
	{
	    return new GuiEditTaskHunt(parent, quest, this);
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
		if(required <= 0)
		{
			return 1F;
		}
		
		return getUsersProgress(uuid) / (float)required;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
	{
	    return new PanelTaskHunt(rect, quest, this);
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
		int total = 0;
		
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
				
				total += getUsersProgress(mem);
			}
		}
		
		return total;
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
	public IJsonDoc getDocumentation()
	{
		return null;
	}
}
