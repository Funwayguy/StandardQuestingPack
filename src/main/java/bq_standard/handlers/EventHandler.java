package bq_standard.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.storage.DBEntry;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.loot.LootRegistry;
import bq_standard.tasks.*;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.commons.lang3.Validate;

import java.util.ArrayDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

@SuppressWarnings("unused")
public class EventHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if(event.entityPlayer == null || event.entityPlayer.worldObj.isRemote || event.isCanceled()) return;
        
		EntityPlayer player = event.entityPlayer;
        QuestCache qc = (QuestCache)player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
		if(qc == null) return;
		
		Block block = player.worldObj.getBlock(event.x, event.y, event.z);
		int meta = player.worldObj.getBlockMetadata(event.x, event.y, event.z);
		boolean isHit = event.action == Action.LEFT_CLICK_BLOCK;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskInteractItem) ((TaskInteractItem)task.getValue()).onInteract(entry, player, player.getHeldItem(), block, meta, event.x, event.y, event.z, isHit);
            }
		}
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityAttack(AttackEntityEvent event)
    {
        if(event.entityPlayer == null || event.target == null || event.entityPlayer.worldObj.isRemote || event.isCanceled()) return;
        
		EntityPlayer player = event.entityPlayer;
        QuestCache qc = (QuestCache)player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskInteractEntity) ((TaskInteractEntity)task.getValue()).onInteract(entry, player, player.getHeldItem(), event.target, true);
            }
		}
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityInteract(EntityInteractEvent event)
    {
        if(event.entityPlayer == null || event.target == null || event.entityPlayer.worldObj.isRemote || event.isCanceled()) return;
        
		EntityPlayer player = event.entityPlayer;
        QuestCache qc = (QuestCache)player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskInteractEntity) ((TaskInteractEntity)task.getValue()).onInteract(entry, player, player.getHeldItem(), event.target, true);
            }
		}
    }
    
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemCrafted(ItemCraftedEvent event)
	{
		if(event.player == null || event.player.worldObj.isRemote) return;
        
        QuestCache qc = (QuestCache)event.player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskCrafting) ((TaskCrafting)task.getValue()).onItemCraft(entry, event.player, event.crafting.copy());
            }
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemSmelted(ItemSmeltedEvent event)
	{
		if(event.player == null || event.player.worldObj.isRemote) return;
        
        QuestCache qc = (QuestCache)event.player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskCrafting) ((TaskCrafting)task.getValue()).onItemSmelt(entry, event.player, event.smelting.copy());
            }
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemAnvil(AnvilRepairEvent event)
	{
		if(event.entityPlayer == null || event.entityPlayer.worldObj.isRemote) return;
        
        QuestCache qc = (QuestCache)event.entityPlayer.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskCrafting) ((TaskCrafting)task.getValue()).onItemAnvil(entry, event.entityPlayer, event.right.copy());
            }
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityKilled(LivingDeathEvent event)
	{
		if(event.source == null || !(event.source.getSourceOfDamage() instanceof EntityPlayer) || event.source.getSourceOfDamage().worldObj.isRemote || event.isCanceled()) return;
		
		EntityPlayer player = (EntityPlayer)event.source.getSourceOfDamage();
        QuestCache qc = (QuestCache)player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskHunt) ((TaskHunt)task.getValue()).onKilledByPlayer(entry, player, event.entityLiving, event.source);
            }
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onBlockBreak(BreakEvent event)
	{
		if(event.getPlayer() == null || event.getPlayer().worldObj.isRemote || event.isCanceled()) return;
		
        QuestCache qc = (QuestCache)event.getPlayer().getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskBlockBreak) ((TaskBlockBreak)task.getValue()).onBlockBreak(entry, event.getPlayer(), event.block, event.blockMetadata, event.x, event.y, event.z);
            }
		}
	}
	
	@SubscribeEvent
    public void onEntityLiving(LivingUpdateEvent event)
    {
        if(!(event.entityLiving instanceof EntityPlayer) || event.entityLiving.worldObj.isRemote || event.entityLiving.ticksExisted%20 != 0 || QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE)) return;
        
        EntityPlayer player = (EntityPlayer)event.entityLiving;
        QuestCache qc = (QuestCache)player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof ITaskTickable)
                {
                    ((ITaskTickable)task.getValue()).tickTask(entry, player);
                }
            }
		}
    }
    
    @SubscribeEvent
    public void onEntityCreated(EntityJoinWorldEvent event)
    {
        if(!(event.entity instanceof EntityPlayer) || event.entity.worldObj.isRemote) return;
        
		PlayerContainerListener.refreshListener((EntityPlayer)event.entity);
    }
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent event)
	{
		if(event.modID.equalsIgnoreCase(BQ_Standard.MODID))
		{
			ConfigHandler.config.save();
			ConfigHandler.initConfigs();
		}
	}
	
	@SubscribeEvent
    public void onPlayerJoin(PlayerLoggedInEvent event)
    {
		if(!event.player.worldObj.isRemote && event.player instanceof EntityPlayerMP)
		{
			LootRegistry.INSTANCE.sendDatabase((EntityPlayerMP)event.player);
		}
    }
	
	@SubscribeEvent
    public void onWorldSave(WorldEvent.Save event)
    {
        if(!event.world.isRemote && LootSaveLoad.INSTANCE.worldDir != null && event.world.provider.dimensionId == 0)
        {
            LootSaveLoad.INSTANCE.SaveLoot();
        }
    }
	
	private static final ArrayDeque<FutureTask> serverTasks = new ArrayDeque<>();
	private static Thread serverThread = null;
	
	// NOTE: This is slightly different to the version in the base mod. This one will not immediately run tasks even if it's from the same thread.
    public static <T> ListenableFuture<T> scheduleServerTask(Callable<T> task)
    {
        Validate.notNull(task);
        
        ListenableFutureTask<T> listenablefuturetask = ListenableFutureTask.create(task);

        synchronized (serverTasks)
        {
            serverTasks.add(listenablefuturetask);
            return listenablefuturetask;
        }
    }
	
	@SubscribeEvent
    public void onServerTick(ServerTickEvent event)
    {
        if(event.phase != Phase.START) return;
        if(serverThread == null) serverThread = Thread.currentThread();
        
        synchronized(serverTasks)
        {
            while(!serverTasks.isEmpty()) serverTasks.poll().run();
        }
    }
}
