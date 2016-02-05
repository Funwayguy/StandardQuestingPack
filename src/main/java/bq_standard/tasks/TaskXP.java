package bq_standard.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;
import org.apache.logging.log4j.Level;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayer;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.JsonHelper;
import bq_standard.XPHelper;
import bq_standard.client.gui.tasks.GuiTaskXP;
import bq_standard.core.BQ_Standard;

public class TaskXP extends TaskBase
{
	public HashMap<UUID, Integer> userProgress = new HashMap<UUID, Integer>();
	public boolean levels = true;
	public int amount = 30;
	public boolean consume = true;
	
	@Override
	public void Update(EntityPlayer player)
	{
		if(!consume && player.ticksExisted%200 == 0 && !QuestDatabase.editMode) // Auto-detect once per second
		{
			Detect(player);
		}
	}
	
	@Override
	public void Detect(EntityPlayer player)
	{
		if(isComplete(player.getUniqueID()))
		{
			return;
		}
		
		Integer progress = userProgress.get(player.getUniqueID());
		progress = progress == null? 0 : progress;
		
		int i = progress + XPHelper.getPlayerXP(player);
		
		if(consume)
		{
			int remaining = amount - progress;
			int change = Math.min(remaining, i);
			progress += change;
			userProgress.put(player.getUniqueID(), progress);
			XPHelper.AddXP(player, -change);
		}
		
		int rawXP = levels? XPHelper.getLevelXP(amount) : amount;
		
		if(i >= rawXP)
		{
			completeUsers.add(player.getUniqueID());
		}
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.xp";
	}
	
	@Override
	public void ResetProgress(UUID uuid)
	{
		completeUsers.remove(uuid);
	}
	
	@Override
	public void ResetAllProgress()
	{
		completeUsers = new ArrayList<UUID>();
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
		amount = JsonHelper.GetNumber(json, "amount", 30).intValue();
		levels = JsonHelper.GetBoolean(json, "isLevels", true);
		consume = JsonHelper.GetBoolean(json, "consume", true);
		
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
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("amount", amount);
		json.addProperty("isLevels", levels);
		json.addProperty("consume", consume);
		
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
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskXP(this, screen, posX, posY, sizeX, sizeY);
	}
	
}
