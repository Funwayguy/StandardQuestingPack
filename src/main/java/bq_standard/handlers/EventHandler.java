package bq_standard.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.storage.DBEntry;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.loot.LootRegistry;
import bq_standard.tasks.ITaskTickable;
import bq_standard.tasks.TaskBlockBreak;
import bq_standard.tasks.TaskCrafting;
import bq_standard.tasks.TaskHunt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class EventHandler
{
	@SubscribeEvent
	public void onItemCrafted(ItemCraftedEvent event)
	{
		if(event.player == null || event.player.world.isRemote) return;
        
        QuestCache qc = event.player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskCrafting) ((TaskCrafting)task.getValue()).onItemCraft(entry.getValue(), event.player, event.crafting.copy());
            }
		}
	}
	
	@SubscribeEvent
	public void onItemSmelted(ItemSmeltedEvent event)
	{
		if(event.player == null || event.player.world.isRemote) return;
        
       QuestCache qc = event.player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskCrafting) ((TaskCrafting)task.getValue()).onItemSmelt(entry.getValue(), event.player, event.smelting.copy());
            }
		}
	}
	
	@SubscribeEvent
	public void onItemAnvil(AnvilRepairEvent event)
	{
		if(event.getEntityPlayer() == null || event.getEntityPlayer().world.isRemote) return;
        
        QuestCache qc = event.getEntityPlayer().getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskCrafting) ((TaskCrafting)task.getValue()).onItemAnvil(entry.getValue(), event.getEntityPlayer(), event.getItemResult().copy());
            }
		}
	}
	
	@SubscribeEvent
	public void onEntityKilled(LivingDeathEvent event)
	{
		if(event.getSource() == null || !(event.getSource().getTrueSource() instanceof EntityPlayer) || event.getSource().getTrueSource().world.isRemote) return;
		
		EntityPlayer player = (EntityPlayer)event.getSource().getTrueSource();
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskHunt) ((TaskHunt)task.getValue()).onKilledByPlayer(entry.getValue(), player, event.getEntityLiving(), event.getSource());
            }
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event)
	{
		if(event.getPlayer() == null || event.getPlayer().world.isRemote) return;
		
        QuestCache qc = event.getPlayer().getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskBlockBreak) ((TaskBlockBreak)task.getValue()).onBlockBreak(entry.getValue(), event.getPlayer(), event.getState(), event.getPos());
            }
		}
	}
	
	@SubscribeEvent
    public void onEntityLiving(LivingUpdateEvent event)
    {
        if(!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote || event.getEntityLiving().ticksExisted%20 != 0 || QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE)) return;
        
        EntityPlayer player = (EntityPlayer)event.getEntityLiving();
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof ITaskTickable)
                {
                    ((ITaskTickable)task.getValue()).tickTask(entry.getValue(), player);
                }
            }
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
