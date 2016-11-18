package bq_standard.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.party.IParty;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.properties.NativeProps;
import betterquesting.api.quests.tasks.IProgression;
import betterquesting.api.quests.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestSettings;
import bq_standard.XPHelper;
import bq_standard.client.gui.tasks.GuiTaskXP;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskXP;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class TaskXP implements ITask, IProgression<Integer>
{
	private ArrayList<UUID> completeUsers = new ArrayList<UUID>();
	public HashMap<UUID, Integer> userProgress = new HashMap<UUID, Integer>();
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
	public void update(EntityPlayer player, IQuest quest)
	{
		if(player.ticksExisted%60 == 0 && !QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE))
		{
			if(!consume)
			{
				setUserProgress(player.getGameProfile().getId(), XPHelper.getPlayerXP(player));
			}
			
			int rawXP = levels? XPHelper.getLevelXP(amount) : amount;
			int totalXP = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? getPartyProgress(player.getGameProfile().getId()) : getGlobalProgress();
			if(totalXP >= rawXP)
			{
				setComplete(player.getGameProfile().getId());
			}
		}
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		if(isComplete(player.getGameProfile().getId()))
		{
			return;
		}
		
		int progress = getUsersProgress(player.getGameProfile().getId());
		int rawXP = levels? XPHelper.getLevelXP(amount) : amount;
		int plrXP = XPHelper.getPlayerXP(player);
		int remaining = rawXP - progress;
		int cost = Math.min(remaining, plrXP);
		
		if(consume)
		{
			progress += cost;
			setUserProgress(player.getGameProfile().getId(), progress);
			XPHelper.AddXP(player, -cost);
		} else
		{
			setUserProgress(player.getGameProfile().getId(), plrXP);
		}
		
		int totalXP = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? getPartyProgress(player.getGameProfile().getId()) : getGlobalProgress();
		if(totalXP >= rawXP)
		{
			setComplete(player.getGameProfile().getId());
		}
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.xp";
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.PROGRESS)
		{
			return this.writeProgressToJson(json);
		} else if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		json.addProperty("amount", amount);
		json.addProperty("isLevels", levels);
		json.addProperty("consume", consume);
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.PROGRESS)
		{
			this.readProgressFromJson(json);
			return;
		} else if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		amount = JsonHelper.GetNumber(json, "amount", 30).intValue();
		levels = JsonHelper.GetBoolean(json, "isLevels", true);
		consume = JsonHelper.GetBoolean(json, "consume", true);
	}
	
	public void readProgressFromJson(JsonObject json)
	{
		completeUsers = new ArrayList<UUID>();
		for(JsonElement entry : JsonHelper.GetArray(json, "completeUsers"))
		{
			if(entry == null || !entry.isJsonPrimitive())
			{
				continue;
			}
			
			try
			{
				completeUsers.add(UUID.fromString(entry.getAsString()));
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load UUID for task", e);
			}
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
	
	public JsonObject writeProgressToJson(JsonObject json)
	{
		JsonArray jArray = new JsonArray();
		for(UUID uuid : completeUsers)
		{
			jArray.add(new JsonPrimitive(uuid.toString()));
		}
		json.add("completeUsers", jArray);
		
		JsonArray progArray = new JsonArray();
		for(Entry<UUID,Integer> entry : userProgress.entrySet())
		{
			JsonObject pJson = new JsonObject();
			pJson.addProperty("uuid", entry.getKey().toString());
			pJson.addProperty("value", entry.getValue());
			progArray.add(pJson);
		}
		json.add("userProgress", progArray);
		
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
		userProgress.clear();;
	}
	
	@Override
	public float getParticipation(UUID uuid)
	{
		int rawXP = !levels? amount : XPHelper.getLevelXP(amount);
		
		if(rawXP <= 0)
		{
			return 1F;
		}
		
		return getUsersProgress(uuid) / (float)rawXP;
	}
	
	@Override
	public IGuiEmbedded getTaskGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new GuiTaskXP(this, quest, posX, posY, sizeX, sizeY);
	}
	
	@Override
	public GuiScreen getTaskEditor(GuiScreen screen, IQuest quest)
	{
		return null;
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
		
		IParty party = PartyManager.INSTANCE.getUserParty(uuid);
		
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
			total += i == null? 0 : 1;
		}
		
		return total;
	}
	
}
