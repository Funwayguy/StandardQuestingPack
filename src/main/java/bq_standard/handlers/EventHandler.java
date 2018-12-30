package bq_standard.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.QuestCache;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.loot.LootRegistry;
import bq_standard.tasks.TaskBlockBreak;
import bq_standard.tasks.TaskCrafting;
import bq_standard.tasks.TaskHunt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

import java.util.Map.Entry;

public class EventHandler
{
	@SubscribeEvent
	public void onItemCrafted(ItemCraftedEvent event)
	{
		if(event.player == null || event.player.world.isRemote)
		{
			return;
		}
		
		for(Entry<TaskCrafting,IQuest> entry : QuestCache.INSTANCE.getActiveTasks(QuestingAPI.getQuestingUUID(event.player), TaskCrafting.class).entrySet())
		{
			entry.getKey().onItemCrafted(entry.getValue(), event.player, event.crafting.copy());
		}
	}
	
	@SubscribeEvent
	public void onItemSmelted(ItemSmeltedEvent event)
	{
		if(event.player == null || event.player.world.isRemote)
		{
			return;
		}
		
		for(Entry<TaskCrafting,IQuest> entry : QuestCache.INSTANCE.getActiveTasks(QuestingAPI.getQuestingUUID(event.player), TaskCrafting.class).entrySet())
		{
			entry.getKey().onItemSmelted(entry.getValue(), event.player, event.smelting.copy());
		}
	}
	
	@SubscribeEvent
	public void onEntityKilled(LivingDeathEvent event)
	{
		if(event.getSource() == null || !(event.getSource().getTrueSource() instanceof EntityPlayer) || event.getSource().getTrueSource().world.isRemote)
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer)event.getSource().getTrueSource();
		
		for(Entry<TaskHunt,IQuest> entry : QuestCache.INSTANCE.getActiveTasks(QuestingAPI.getQuestingUUID(player), TaskHunt.class).entrySet())
		{
			entry.getKey().onKilledByPlayer(entry.getValue(), event.getEntityLiving(), event.getSource());
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event)
	{
		if(event.getPlayer() == null || event.getPlayer().world.isRemote)
		{
			return;
		}
		
		for(Entry<TaskBlockBreak,IQuest> entry : QuestCache.INSTANCE.getActiveTasks(QuestingAPI.getQuestingUUID(event.getPlayer()), TaskBlockBreak.class).entrySet())
		{
			entry.getKey().onBlockBreak(entry.getValue(), event.getPlayer(), event.getState(), event.getPos());
		}
	}
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent event)
	{
		if(event.getModID().equalsIgnoreCase(BQ_Standard.MODID))
		{
			ConfigHandler.config.save();
			ConfigHandler.initConfigs();
		}
	}
	
	@SubscribeEvent
    public void onPlayerJoin(PlayerLoggedInEvent event)
    {
		if(!event.player.world.isRemote && event.player instanceof EntityPlayerMP)
		{
			LootRegistry.INSTANCE.sendDatabase((EntityPlayerMP)event.player);
		}
    }
	
	@SubscribeEvent
    public void onWorldSave(WorldEvent.Save event)
    {
        if(!event.getWorld().isRemote && LootSaveLoad.INSTANCE.worldDir != null && event.getWorld().provider.getDimension() == 0)
        {
            LootSaveLoad.INSTANCE.SaveLoot();
        }
    }
}
