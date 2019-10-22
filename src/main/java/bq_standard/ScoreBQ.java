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

public class ScoreBQ implements INBTPartial<NBTTagList, UUID>
{
	private final HashMap<UUID, Integer> playerScores = new HashMap<>();
	
	public int getScore(@Nonnull UUID uuid)
	{
		Integer value = playerScores.get(uuid);
		return value == null? 0 : value;
	}
	
	public void setScore(@Nonnull UUID uuid, int value)
	{
		playerScores.put(uuid, value);
	}
	
	public boolean hasEntry(@Nonnull UUID uuid)
    {
        return playerScores.containsKey(uuid);
    }
	
	@Override
	public NBTTagList writeToNBT(NBTTagList nbt, @Nullable List<UUID> subset)
	{
		for(Entry<UUID, Integer> entry : playerScores.entrySet())
		{
		    if(subset != null && !subset.contains(entry.getKey())) continue;
			NBTTagCompound jObj = new NBTTagCompound();
			jObj.setString("uuid", entry.getKey().toString());
			jObj.setInteger("value", entry.getValue());
			nbt.appendTag(jObj);
		}
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagList nbt, boolean merge)
	{
        if(!merge) playerScores.clear();
		
		for(int i = 0; i < nbt.tagCount(); i++)
		{
			try
			{
			    NBTTagCompound jObj = nbt.getCompoundTagAt(i);
				playerScores.put(UUID.fromString(jObj.getString("uuid")), jObj.getInteger("value"));
				
			} catch(Exception ignored){}
		}
	}
}
