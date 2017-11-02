package bq_standard.rewards.loot;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.Level;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import bq_standard.core.BQ_Standard;
import bq_standard.network.StandardPacketType;
import com.google.gson.JsonObject;

public class LootRegistry
{
	public static CopyOnWriteArrayList<LootGroup> lootGroups = new CopyOnWriteArrayList<LootGroup>();
	public static boolean updateUI = false;
	
	public static void registerGroup(LootGroup group)
	{
		if(group == null || lootGroups.contains(group))
		{
			return;
		}
		
		lootGroups.add(group);
	}
	
	/**
	 * 
	 * @param weight A value between 0 and 1 that represents how common this reward is (i.e. higher values mean rarer loot)
	 * @param rand
	 * @return a loot group with the corresponding rarity of loot
	 */
	public static LootGroup getWeightedGroup(float weight, Random rand)
	{
		int total = getTotalWeight();
		
		if(total <= 0)
		{
			BQ_Standard.logger.log(Level.WARN, "Unable to get random loot group! Reason: No registered groups/weights");
			return null;
		}
		
		float r = rand.nextFloat() * total/4F + weight*total*0.75F;
		int cnt = 0;
		
		ArrayList<LootGroup> sorted = new ArrayList<LootGroup>(lootGroups);
		Collections.sort(sorted);
		
		for(LootGroup entry : sorted)
		{
			cnt += entry.weight;
			if(cnt >= r)
			{
				return entry;
			}
		}
		
		BQ_Standard.logger.log(Level.WARN, "Unable to get random loot group! Reason: Unknown");
		return null;
	}
	
	public static int getTotalWeight()
	{
		int i = 0;
		
		for(LootGroup group : lootGroups)
		{
			i += group.weight;
		}
		
		return i;
	}
	
	public static ArrayList<BigItemStack> getStandardLoot(EntityPlayer player)
	{
		ArrayList<BigItemStack> stacks = new ArrayList<BigItemStack>();
		
		LootTable table = player.worldObj.getLootTableManager().getLootTableFromLocation(LootTableList.CHESTS_SIMPLE_DUNGEON);
		
		LootContext.Builder lcBuilder = new LootContext.Builder((WorldServer)player.worldObj);
		for(ItemStack s : table.generateLootForPools(player.getRNG(), lcBuilder.build()))
		{
			stacks.add(new BigItemStack(s));
		}
		
		return stacks;
	}
	
	public static void updateClients()
	{
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound json = new NBTTagCompound();
		LootRegistry.writeToJson(json);
		tags.setTag("Database", json);
		QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToAll(new QuestingPacket(StandardPacketType.LOOT_SYNC.GetLocation(), tags));
	}
	
	public static void sendDatabase(EntityPlayerMP player)
	{
		NBTTagCompound tags = new NBTTagCompound();
		NBTTagCompound json = new NBTTagCompound();
		LootRegistry.writeToJson(json);
		tags.setTag("Database", json);
		QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToPlayer(new QuestingPacket(StandardPacketType.LOOT_SYNC.GetLocation(), tags), player);
	}
	
	public static void writeToJson(NBTTagCompound json)
	{
		NBTTagList jRew = new NBTTagList();
		for(LootGroup entry : lootGroups)
		{
			NBTTagCompound jGrp = new NBTTagCompound();
			entry.writeToJson(jGrp);
			jRew.appendTag(jGrp);
		}
		json.setTag("groups", jRew);
	}
	
	public static void readFromJson(NBTTagCompound json)
	{
		lootGroups.clear();
		NBTTagList list = json.getTagList("groups", 10);
		for(int i = 0; i < list.tagCount(); i++)
		{
			NBTBase entry = list.get(i);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			LootGroup group = new LootGroup();
			group.readFromJson((NBTTagCompound)entry);
			
			lootGroups.add(group);
		}
		
		updateUI = true;
	}
	
	static File worldDir = null;
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event)
	{
		if(event.getWorld().isRemote || worldDir != null)
		{
			return;
		}
		
		MinecraftServer server = event.getWorld().getMinecraftServer();
		
		if(BQ_Standard.proxy.isClient())
		{
			worldDir = server.getFile("saves/" + server.getFolderName());
		} else
		{
			worldDir = server.getFile(server.getFolderName());
		}
    	
    	File f1 = new File(worldDir, "QuestLoot.json");
		JsonObject j1 = new JsonObject();
		
		if(f1.exists())
		{
			j1 = JsonHelper.ReadFromFile(f1);
		} else
		{
			f1 = server.getFile("config/betterquesting/DefaultLoot.json");
			
			if(f1.exists())
			{
				j1 = JsonHelper.ReadFromFile(f1);
			}
		}
		
		readFromJson(NBTConverter.JSONtoNBT_Object(j1, new NBTTagCompound(), true));
	}
	
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event)
	{
		if(!event.getWorld().isRemote && worldDir != null && event.getWorld().provider.getDimension() == 0)
		{
			NBTTagCompound jsonQ = new NBTTagCompound();
			writeToJson(jsonQ);
			JsonHelper.WriteToFile(new File(worldDir, "QuestLoot.json"), NBTConverter.NBTtoJSON_Compound(jsonQ, new JsonObject(), true));
		}
	}
	
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event)
	{
		if(!event.getWorld().isRemote && !event.getWorld().getMinecraftServer().isServerRunning())
		{
			worldDir = null;
		}
	}
	
	@SubscribeEvent
	public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(!event.player.worldObj.isRemote && event.player instanceof EntityPlayerMP)
		{
			sendDatabase((EntityPlayerMP)event.player);
		}
	}
}
