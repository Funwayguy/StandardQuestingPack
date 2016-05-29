package bq_standard.tasks;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyManager;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.advanced.AdvancedTaskBase;
import betterquesting.quests.tasks.advanced.IProgressionTask;
import betterquesting.utils.ItemComparison;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import bq_standard.client.gui.editors.GuiHuntEditor;
import bq_standard.client.gui.tasks.GuiTaskHunt;
import bq_standard.core.BQ_Standard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TaskHunt extends AdvancedTaskBase implements IProgressionTask<Integer>
{
	public HashMap<UUID, Integer> userProgress = new HashMap<UUID, Integer>();
	public String idName = "Zombie";
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
		return "bq_standard.task.hunt";
	}
	
	@Override
	public void Update(QuestInstance quest, EntityPlayer player)
	{
		if(player.ticksExisted%200 == 0 && !QuestDatabase.editMode)
		{
			Detect(quest, player);
		}
	}
	
	@Override
	public void Detect(QuestInstance quest, EntityPlayer player)
	{
		if(isComplete(player.getUniqueID()))
		{
			return;
		}
		
		int progress = quest == null || !quest.globalQuest? GetPartyProgress(player.getUniqueID()) : GetGlobalProgress();
		
		if(progress >= required)
		{
			setCompletion(player.getUniqueID(), true);
		}
	}
	
	@Override
	public void onKilledByPlayer(QuestInstance quest, EntityLivingBase entity, DamageSource source)
	{
		EntityPlayer player = (EntityPlayer)source.getEntity();
		
		if(player == null || entity == null || this.isComplete(player.getUniqueID()))
		{
			return;
		}
		
		int progress = GetUserProgress(player.getUniqueID());
		
		Class<? extends Entity> subject = entity.getClass();
		Class<? extends Entity> target = (Class<? extends Entity>)EntityList.stringToClassMapping.get(idName);
		
		if(subject == null || target == null)
		{
			return; // Missing necessary data
		} else if(subtypes && !target.isAssignableFrom(subject))
		{
			return; // This is not the intended target or sub-type
		} else if(!subtypes && !EntityList.getEntityString(entity).equals(idName))
		{
			return; // This isn't the exact target required
		}
		
		NBTTagCompound subjectTags = new NBTTagCompound();
		entity.writeToNBTOptional(subjectTags);
		if(!ignoreNBT && !ItemComparison.CompareNBTTag(targetTags, subjectTags, true))
		{
			return;
		}
		
		SetUserProgress(player.getUniqueID(), progress + 1);
		
		Detect(quest, player);
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("target", idName);
		json.addProperty("required", required);
		json.addProperty("subtypes", subtypes);
		json.addProperty("ignoreNBT", ignoreNBT);
		json.add("targetNBT", NBTConverter.NBTtoJSON_Compound(targetTags, new JsonObject()));
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
		idName = JsonHelper.GetString(json, "target", "Zombie");
		required = JsonHelper.GetNumber(json, "required", 1).intValue();
		subtypes = JsonHelper.GetBoolean(json, "subtypes", true);
		ignoreNBT = JsonHelper.GetBoolean(json, "ignoreNBT", true);
		targetTags = NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(json, "targetNBT"), new NBTTagCompound());
		
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
		
		userProgress = new HashMap<UUID,Integer>();
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
			
			userProgress.put(uuid, JsonHelper.GetNumber(entry.getAsJsonObject(), "value", 0).intValue());
		}
	}
	
	@Override
	public void writeProgressToJson(JsonObject json)
	{
		super.writeProgressToJson(json);
		
		JsonArray progArray = new JsonArray();
		for(Entry<UUID,Integer> entry : userProgress.entrySet())
		{
			JsonObject pJson = new JsonObject();
			pJson.addProperty("uuid", entry.getKey().toString());
			pJson.addProperty("value", entry.getValue());
			progArray.add(pJson);
		}
		json.add("userProgress", progArray);
	}
	
	/**
	 * Returns a new editor screen for this Reward type to edit the given data
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen GetEditor(GuiScreen parent, JsonObject data)
	{
		return new GuiHuntEditor(parent, data);
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
		userProgress = new HashMap<UUID,Integer>();
	}
	
	@Override
	public float GetParticipation(UUID uuid)
	{
		if(required <= 0)
		{
			return 1F;
		}
		
		return GetUserProgress(uuid) / (float)required;
	}

	@Override
	public GuiEmbedded getGui(QuestInstance quest, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskHunt(quest, this, screen, posX, posY, sizeX, sizeY);
	}
	
	@Override
	public void SetUserProgress(UUID uuid, Integer progress)
	{
		userProgress.put(uuid, progress);
	}
	
	@Override
	public Integer GetUserProgress(UUID uuid)
	{
		Integer progress = userProgress.get(uuid);
		return progress == null? 0 : progress;
	}
	
	@Override
	public Integer GetPartyProgress(UUID uuid)
	{
		int total = 0;
		
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
				
				total += GetUserProgress(mem.userID);
			}
		}
		
		return total;
	}
	
	@Override
	public Integer GetGlobalProgress()
	{
		int total = 0;
		
		for(Integer i : userProgress.values())
		{
			total += i == null? 0 : i;
		}
		
		return total;
	}
}
