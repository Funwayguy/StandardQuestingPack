package bq_standard;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ScoreBQ
{
	HashMap<UUID, Integer> playerScores = new HashMap<UUID, Integer>();
	
	public int getScore(UUID uuid)
	{
		Integer value = playerScores.get(uuid);
		return value == null? 0 : value;
	}
	
	public void setScore(UUID uuid, int value)
	{
		playerScores.put(uuid, value);
	}
	
	public void writeJson(JsonObject json)
	{
		JsonArray ary = new JsonArray();
		for(Entry<UUID,Integer> entry : playerScores.entrySet())
		{
			JsonObject jObj = new JsonObject();
			jObj.addProperty("uuid", entry.getKey().toString());
			jObj.addProperty("value", entry.getValue());
			ary.add(jObj);
		}
		json.add("scores", ary);
	}
	
	public void readJson(JsonObject json)
	{
		playerScores.clear();
		for(JsonElement element : JsonHelper.GetArray(json, "scores"))
		{
			if(element == null || !element.isJsonObject())
			{
				continue;
			}
			
			JsonObject jObj = element.getAsJsonObject();
			try
			{
				UUID uuid = UUID.fromString(JsonHelper.GetString(jObj, "uuid", ""));
				playerScores.put(uuid, JsonHelper.GetNumber(jObj, "value", 0).intValue());
				
			} catch(Exception e)
			{
				continue;
			}
		}
	}
}
