package bq_standard.rewards.loot;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.INBTSaveLoad;
import betterquesting.api2.storage.SimpleDatabase;
import bq_standard.network.StandardPacketType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.ChestGenHooks;

import java.util.*;

public class LootRegistry extends SimpleDatabase<LootGroup> implements INBTSaveLoad<NBTTagCompound>
{
    // TODO: Use proper encapsulation
    // TODO: Add localised group names
    // TODO: Use a better UI updating method
    // TODO: Add claim limits and store by UUID
    
    public static final LootRegistry INSTANCE = new LootRegistry();
    
    private final Comparator<DBEntry<LootGroup>> groupSorter = Comparator.comparingInt(o -> o.getValue().weight);
	public boolean updateUI = false;
    
    public int getTotalWeight()
    {
        DBEntry<LootGroup>[] groups = this.getEntries();
        
        int i = 0;
        
        for(DBEntry<LootGroup> lg : groups)
        {
            i += lg.getValue().weight;
        }
        
        return i;
    }
    
    /**
	 *
	 * @param weight A value between 0 and 1 that represents how common this reward is (i.e. higher values mean rarer loot)
	 * @param rand The random instance used to pick the group
	 * @return a loot group with the corresponding rarity of loot
	 */
    public LootGroup getWeightedGroup(float weight, Random rand)
    {
        final int total = getTotalWeight();
        
        if(total <= 0) return null;
		
		float r = rand.nextFloat() * total/4F + weight*total*0.75F;
		int cnt = 0;
		
		DBEntry<LootGroup>[] sorted = getEntries();
        Arrays.sort(sorted, groupSorter);
		
		for(DBEntry<LootGroup> entry : sorted)
		{
			cnt += entry.getValue().weight;
			if(cnt >= r)
			{
				return entry.getValue();
			}
		}
		
		return null;
    }
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag)
    {
		NBTTagList jRew = new NBTTagList();
		for(DBEntry<LootGroup> entry : getEntries())
		{
			NBTTagCompound jGrp = entry.getValue().writeToNBT(new NBTTagCompound());
			jGrp.setInteger("ID", entry.getID());
			jRew.appendTag(jGrp);
		}
		tag.setTag("groups", jRew);
		
        return tag;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
		this.reset();
		
		List<LootGroup> legacyGroups = new ArrayList<>();
		
		NBTTagList list = tag.getTagList("groups", 10);
		for(int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound entry = list.getCompoundTagAt(i);
			int id = entry.hasKey("ID", 99) ? entry.getInteger("ID") : -1;
			
			LootGroup group = new LootGroup();
			group.readFromNBT(entry);
			
			if(id >= 0)
            {
                this.add(id, group);
            } else
            {
                legacyGroups.add(group);
            }
		}
		
		for(LootGroup group : legacyGroups)
        {
            this.add(this.nextID(), group);
        }
		
		updateUI = true;
    }
	
	public void updateClients()
	{
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound json = new NBTTagCompound();
		LootRegistry.INSTANCE.writeToNBT(json);
		tags.setTag("Database", json);
		QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToAll(new QuestingPacket(StandardPacketType.LOOT_SYNC.GetLocation(), tags));
	}
	
	public void sendDatabase(EntityPlayerMP player)
	{
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound json = new NBTTagCompound();
		LootRegistry.INSTANCE.writeToNBT(json);
		tags.setTag("Database", json);
		QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToPlayer(new QuestingPacket(StandardPacketType.LOOT_SYNC.GetLocation(), tags), player);
	}
	
	public static List<BigItemStack> getStandardLoot(EntityPlayer player)
	{
		List<BigItemStack> stacks = new ArrayList<>();
		
		int i = 1 + player.getRNG().nextInt(7);
		while(i > 0)
        {
            stacks.add(new BigItemStack(ChestGenHooks.getOneItem(ChestGenHooks.DUNGEON_CHEST, player.getRNG())));
            i--;
        }
		
		return stacks;
	}
}
