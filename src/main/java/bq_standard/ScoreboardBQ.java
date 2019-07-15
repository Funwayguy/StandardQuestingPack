package bq_standard;

import betterquesting.api2.storage.INBTPartial;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class ScoreboardBQ implements INBTPartial<NBTTagList, UUID>
{
    public static final ScoreboardBQ INSTANCE = new ScoreboardBQ();
    
	private final HashMap<String, ScoreBQ> objectives = new HashMap<>();
	
	public synchronized int getScore(@Nonnull UUID uuid, @Nonnull String scoreName)
	{
		ScoreBQ score = objectives.get(scoreName);
		return score == null ? 0 : score.getScore(uuid);
	}
	
	public synchronized void setScore(@Nonnull UUID uuid, @Nonnull String scoreName, int value)
	{
		ScoreBQ score = objectives.computeIfAbsent(scoreName, (key) -> new ScoreBQ());
		score.setScore(uuid, value);
	}
	
	@Override
	public synchronized void readFromNBT(NBTTagList json, boolean merge)
	{
        if(!merge) objectives.clear();
		
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTTagCompound jObj = json.getCompoundTagAt(i);
			ScoreBQ score = new ScoreBQ();
			score.readFromNBT(jObj.getTagList("scores", 10), merge);
			objectives.put(jObj.getString("name"), score);
		}
	}
	
	@Override
	public synchronized NBTTagList writeToNBT(NBTTagList json, @Nullable List<UUID> subset)
	{
		for(Entry<String,ScoreBQ> entry : objectives.entrySet())
		{
			NBTTagCompound jObj = new NBTTagCompound();
			jObj.setString("name", entry.getKey());
			jObj.setTag("scores", entry.getValue().writeToNBT(new NBTTagList(), subset));
			json.appendTag(jObj);
		}
		
		return json;
	}
}
