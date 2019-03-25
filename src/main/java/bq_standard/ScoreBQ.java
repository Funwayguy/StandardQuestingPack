package bq_standard;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class ScoreBQ
{
	private HashMap<UUID, Integer> playerScores = new HashMap<>();
	
	public int getScore(UUID uuid)
	{
		Integer value = playerScores.get(uuid);
		return value == null? 0 : value;
	}
	
	public void setScore(UUID uuid, int value)
	{
		playerScores.put(uuid, value);
	}
	
	public NBTTagList writeJson(NBTTagList json)
	{
		for(Entry<UUID,Integer> entry : playerScores.entrySet())
		{
			NBTTagCompound jObj = new NBTTagCompound();
			jObj.setString("uuid", entry.getKey().toString());
			jObj.setInteger("value", entry.getValue());
			json.appendTag(jObj);
		}
		
		return json;
	}
	
	public void readJson(NBTTagList json)
	{
		playerScores.clear();
		
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase element = json.get(i);
			
			if(element.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound jObj = json.getCompoundTagAt(i);
			
			try
			{
				playerScores.put(UUID.fromString(jObj.getString("uuid")), jObj.getInteger("value"));
				
			} catch(Exception e)
			{
			}
		}
	}
}
