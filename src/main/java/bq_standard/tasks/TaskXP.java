package bq_standard.tasks;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyManager;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.quests.tasks.advanced.IProgressionTask;
import betterquesting.utils.JsonHelper;
import bq_standard.XPHelper;
import bq_standard.client.gui.tasks.GuiTaskXP;
import bq_standard.core.BQ_Standard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TaskXP extends TaskBase implements IProgressionTask<Integer>
{
	public HashMap<UUID, Integer> userProgress = new HashMap<UUID, Integer>();
	public boolean levels = true;
	public int amount = 30;
	public boolean consume = true;
	
	@Override
	public void Update(QuestInstance quest, EntityPlayer player)
	{
		if(player.ticksExisted%60 == 0 && !QuestDatabase.editMode)
		{
			if(!consume)
			{
				SetUserProgress(player.getUniqueID(), XPHelper.getPlayerXP(player));
			}
			
			int rawXP = levels? XPHelper.getLevelXP(amount) : amount;
			int totalXP = quest == null || !quest.globalQuest? GetPartyProgress(player.getUniqueID()) : GetGlobalProgress();
			if(totalXP >= rawXP)
			{
				setCompletion(player.getUniqueID(), true);
			}
		}
	}
	
	@Override
	public void Detect(QuestInstance quest, EntityPlayer player)
	{
		if(isComplete(player.getUniqueID()))
		{
			return;
		}
		
		int progress = GetUserProgress(player.getUniqueID());
		int rawXP = levels? XPHelper.getLevelXP(amount) : amount;
		int plrXP = XPHelper.getPlayerXP(player);
		int remaining = rawXP - progress;
		int cost = Math.min(remaining, plrXP);
		
		if(consume)
		{
			progress += cost;
			SetUserProgress(player.getUniqueID(), progress);
			XPHelper.AddXP(player, -cost);
		} else
		{
			SetUserProgress(player.getUniqueID(), plrXP);
		}
		
		int totalXP = quest == null || !quest.globalQuest? GetPartyProgress(player.getUniqueID()) : GetGlobalProgress();
		if(totalXP >= rawXP)
		{
			setCompletion(player.getUniqueID(), true);
		}
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.xp";
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("amount", amount);
		json.addProperty("isLevels", levels);
		json.addProperty("consume", consume);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
		amount = JsonHelper.GetNumber(json, "amount", 30).intValue();
		levels = JsonHelper.GetBoolean(json, "isLevels", true);
		consume = JsonHelper.GetBoolean(json, "consume", true);
		
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
		userProgress = new HashMap<UUID, Integer>();
	}
	
	@Override
	public float GetParticipation(UUID uuid)
	{
		int rawXP = !levels? amount : XPHelper.getLevelXP(amount);
		
		if(rawXP <= 0)
		{
			return 1F;
		}
		
		return GetUserProgress(uuid) / (float)rawXP;
	}
	
	@Override
	public GuiEmbedded getGui(QuestInstance quest, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskXP(quest, this, screen, posX, posY, sizeX, sizeY);
	}
	
	@Override
	public void SetUserProgress(UUID uuid, Integer progress)
	{
		userProgress.put(uuid, progress);
	}
	
	@Override
	public Integer GetUserProgress(UUID uuid)
	{
		Integer i = userProgress.get(uuid);
		return i == null? 0 : i;
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
			total += i == null? 0 : 1;
		}
		
		return total;
	}
}