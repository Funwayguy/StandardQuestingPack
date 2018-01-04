package bq_standard.rewards.loot;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;

public class LootGroup implements Comparable<LootGroup>
{
	public String name = "Loot Group";
	public int weight = 1;
	public ArrayList<LootEntry> lootEntry = new ArrayList<LootEntry>();
	
	public ArrayList<BigItemStack> getRandomReward(Random rand)
	{
		int total = getTotalWeight();
		float r = rand.nextFloat() * total;
		int cnt = 0;
		
		for(LootEntry entry : lootEntry)
		{
			cnt += entry.weight;
			if(cnt >= r)
			{
				return entry.items;
			}
		}
		
		return new ArrayList<BigItemStack>();
	}
	
	public int getTotalWeight()
	{
		int i = 0;
		
		for(LootEntry entry : lootEntry)
		{
			i += entry.weight;
		}
		
		return i;
	}
	
	public void readFromJson(NBTTagCompound json)
	{
		name = json.hasKey("name", 8) ? json.getString("name") : "Loot Group";
		weight = json.getInteger("weight");;
		weight = Math.max(1, weight);
		
		lootEntry = new ArrayList<LootEntry>();
		NBTTagList jRew = json.getTagList("rewards", 10);
		for(int i = 0; i < jRew.tagCount(); i++)
		{
			NBTBase entry = jRew.get(i);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			LootEntry loot = new LootEntry();
			loot.readFromJson((NBTTagCompound)entry);
			lootEntry.add(loot);
		}
	}
	
	public void writeToJson(NBTTagCompound json)
	{
		json.setString("name", name);
		json.setInteger("weight", weight);
		
		NBTTagList jRew = new NBTTagList();
		for(LootEntry entry : lootEntry)
		{
			if(entry == null)
			{
				continue;
			}
			
			NBTTagCompound jLoot = new NBTTagCompound();
			entry.writeToJson(jLoot);
			jRew.appendTag(jLoot);
		}
		json.setTag("rewards", jRew);
	}

	@Override
	public int compareTo(LootGroup group)
	{
		return (int)Math.signum(group.weight - weight);
	}
	
	public static class LootEntry implements Comparable<LootEntry>
	{
		public int weight = 1;
		public ArrayList<BigItemStack> items = new ArrayList<BigItemStack>();
		
		public void readFromJson(NBTTagCompound json)
		{
			weight = json.getInteger("weight");
			weight = Math.max(1, weight);
			
			items = new ArrayList<BigItemStack>();
			NBTTagList jItm = json.getTagList("items", 10);
			for(int i = 0; i < jItm.tagCount(); i++)
			{
				NBTBase entry = jItm.get(i);
				
				if(entry == null || entry.getId() != 10)
				{
					continue;
				}
				
				BigItemStack stack = JsonHelper.JsonToItemStack((NBTTagCompound)entry);
				
				if(stack != null)
				{
					items.add(stack);
				}
			}
		}
		
		public void writeToJson(NBTTagCompound json)
		{
			json.setInteger("weight", weight);
			
			NBTTagList jItm = new NBTTagList();
			for(BigItemStack stack : items)
			{
				jItm.appendTag(JsonHelper.ItemStackToJson(stack, new NBTTagCompound()));
			}
			json.setTag("items", jItm);
		}

		@Override
		public int compareTo(LootEntry entry)
		{
			return (int)Math.signum(entry.weight - weight);
		}
	}
}
