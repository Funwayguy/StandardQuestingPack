package bq_standard;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import bq_standard.network.StandardPacketType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardBQ
{
	static ConcurrentHashMap<String, ScoreBQ> objectives = new ConcurrentHashMap<>();
	
	public static int getScore(UUID uuid, String scoreName)
	{
		ScoreBQ score = objectives.get(scoreName);
		
		if(score == null)
		{
			return 0;
		} else
		{
			return score.getScore(uuid);
		}
	}
	
	public static void setScore(EntityPlayer player, String scoreName, int value)
	{
		ScoreBQ score = objectives.get(scoreName);
		
		if(score == null)
		{
			score = new ScoreBQ();
			objectives.put(scoreName, score);
		}
		
		score.setScore(QuestingAPI.getQuestingUUID(player), value);
		
		if(player instanceof EntityPlayerMP)
		{
			SendToClient((EntityPlayerMP)player);
		}
	}
	
	public static void SendToClient(EntityPlayerMP player)
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setTag("data", writeJson(new NBTTagList()));
		QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToPlayer(new QuestingPacket(StandardPacketType.SCORE_SYNC.GetLocation(), tags), player);
	}
	
	public static void readJson(NBTTagList json)
	{
		objectives.clear();
		
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase element = json.get(i);
			
			if(element.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound jObj = json.getCompoundTagAt(i);
			String name = jObj.getString("name");
			
			if(name.length() <= 0)
			{
				continue;
			}
			
			ScoreBQ score = new ScoreBQ();
			score.readJson(jObj.getTagList("scores", 10));
			objectives.put(name, score);
		}
	}
	
	public static NBTTagList writeJson(NBTTagList json)
	{
		for(Entry<String,ScoreBQ> entry : objectives.entrySet())
		{
			NBTTagCompound jObj = new NBTTagCompound();
			jObj.setString("name", entry.getKey());
			jObj.setTag("scores", entry.getValue().writeJson(new NBTTagList()));
			json.appendTag(jObj);
		}
		
		return json;
	}
}
