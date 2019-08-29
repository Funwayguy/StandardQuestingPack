package bq_standard.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.storage.DBEntry;
import bq_standard.advancment_hacks.AdvListenerManager;
import bq_standard.network.handlers.NetLootSync;
import bq_standard.network.handlers.NetTaskInteract;
import bq_standard.tasks.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import javax.annotation.Nonnull;
import java.util.*;

public class EventHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRightClickItem(RightClickItem event)
    {
        if(event.getEntityPlayer() == null || event.getEntityLiving().world.isRemote || event.isCanceled()) return;
        
		EntityPlayer player = event.getEntityPlayer();
		
		List<EntityPlayer> actParty = getActiveParty(player);
		List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(getSharedQuests(actParty));
		List<Integer> dirty = new ArrayList<>();
		
		for(DBEntry<IQuest> entry : actQuest)
		{
		    Runnable callback = () -> dirty.add(entry.getID());
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskInteractItem) ((TaskInteractItem)task.getValue()).onInteract(player, event.getHand(), event.getItemStack(), Blocks.AIR.getDefaultState(), event.getPos(), false, callback);
            }
		}
		
		if(dirty.size() > 0) bulkMarkDirtyPlayer(actParty, dirty);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRightClickBlock(RightClickBlock event)
    {
        if(event.getEntityPlayer() == null || event.getEntityLiving().world.isRemote || event.isCanceled()) return;
        
		EntityPlayer player = event.getEntityPlayer();
		
		List<EntityPlayer> actParty = getActiveParty(player);
		List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(getSharedQuests(actParty));
		List<Integer> dirty = new ArrayList<>();
		
		IBlockState state = player.world.getBlockState(event.getPos());
		
		for(DBEntry<IQuest> entry : actQuest)
		{
		    Runnable callback = () -> dirty.add(entry.getID());
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskInteractItem) ((TaskInteractItem)task.getValue()).onInteract(player, event.getHand(), event.getItemStack(), state, event.getPos(), false, callback);
            }
		}
		
		if(dirty.size() > 0) bulkMarkDirtyPlayer(actParty, dirty);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLeftClickBlock(LeftClickBlock event)
    {
        if(event.getEntityPlayer() == null || event.getEntityLiving().world.isRemote || event.isCanceled()) return;
        
		EntityPlayer player = event.getEntityPlayer();
		
		List<EntityPlayer> actParty = getActiveParty(player);
		List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(getSharedQuests(actParty));
		List<Integer> dirty = new ArrayList<>();
		
		IBlockState state = player.world.getBlockState(event.getPos());
		
		for(DBEntry<IQuest> entry : actQuest)
		{
		    Runnable callback = () -> dirty.add(entry.getID());
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskInteractItem) ((TaskInteractItem)task.getValue()).onInteract(player, event.getHand(), event.getItemStack(), state, event.getPos(), true, callback);
            }
		}
		
		if(dirty.size() > 0) bulkMarkDirtyPlayer(actParty, dirty);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRightClickEmpty(RightClickEmpty event) // CLIENT SIDE ONLY EVENT
    {
        if(event.getEntityPlayer() == null || !event.getEntityLiving().world.isRemote || event.isCanceled()) return;
        NetTaskInteract.requestInteraction(false, event.getHand() == EnumHand.MAIN_HAND);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLeftClickAir(LeftClickEmpty event) // CLIENT SIDE ONLY EVENT
    {
        if(event.getEntityPlayer() == null || !event.getEntityLiving().world.isRemote || event.isCanceled()) return;
        NetTaskInteract.requestInteraction(true, true);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityAttack(AttackEntityEvent event)
    {
        if(event.getEntityPlayer() == null || event.getTarget() == null || event.getEntityPlayer().world.isRemote || event.isCanceled()) return;
        
		EntityPlayer player = event.getEntityPlayer();
		
		List<EntityPlayer> actParty = getActiveParty(player);
		List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(getSharedQuests(actParty));
		List<Integer> dirty = new ArrayList<>();
		
		for(DBEntry<IQuest> entry : actQuest)
		{
		    Runnable callback = () -> dirty.add(entry.getID());
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskInteractEntity) ((TaskInteractEntity)task.getValue()).onInteract(player, EnumHand.MAIN_HAND, player.getHeldItemMainhand(), event.getTarget(), true, callback);
            }
		}
		
		if(dirty.size() > 0) bulkMarkDirtyPlayer(actParty, dirty);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityInteract(EntityInteract event)
    {
        if(event.getEntityPlayer() == null || event.getTarget() == null || event.getEntityPlayer().world.isRemote || event.isCanceled()) return;
        
		EntityPlayer player = event.getEntityPlayer();
		
		List<EntityPlayer> actParty = getActiveParty(player);
		List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(getSharedQuests(actParty));
		List<Integer> dirty = new ArrayList<>();
		
		for(DBEntry<IQuest> entry : actQuest)
		{
		    Runnable callback = () -> dirty.add(entry.getID());
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskInteractEntity) ((TaskInteractEntity)task.getValue()).onInteract(player, event.getHand(), event.getItemStack(), event.getTarget(), false, callback);
            }
		}
		
		if(dirty.size() > 0) bulkMarkDirtyPlayer(actParty, dirty);
    }
    
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemCrafted(ItemCraftedEvent event)
	{
		if(event.player == null || event.player.world.isRemote) return;
		
		List<EntityPlayer> actParty = getActiveParty(event.player);
		List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(getSharedQuests(actParty));
		List<Integer> dirty = new ArrayList<>();
		
		for(DBEntry<IQuest> entry : actQuest)
		{
		    Runnable callback = () -> dirty.add(entry.getID());
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskCrafting) ((TaskCrafting)task.getValue()).onItemCraft(event.player, event.crafting.copy(), callback);
            }
		}
		
		if(dirty.size() > 0) bulkMarkDirtyPlayer(actParty, dirty);
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemSmelted(ItemSmeltedEvent event)
	{
		if(event.player == null || event.player.world.isRemote) return;
		
		List<EntityPlayer> actParty = getActiveParty(event.player);
		List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(getSharedQuests(actParty));
		List<Integer> dirty = new ArrayList<>();
		
		for(DBEntry<IQuest> entry : actQuest)
		{
		    Runnable callback = () -> dirty.add(entry.getID());
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskCrafting) ((TaskCrafting)task.getValue()).onItemSmelt(event.player, event.smelting.copy(), callback);
            }
		}
		
		if(dirty.size() > 0) bulkMarkDirtyPlayer(actParty, dirty);
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onItemAnvil(AnvilRepairEvent event)
	{
		if(event.getEntityPlayer() == null || event.getEntityPlayer().world.isRemote) return;
		
		List<EntityPlayer> actParty = getActiveParty(event.getEntityPlayer());
		List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(getSharedQuests(actParty));
		List<Integer> dirty = new ArrayList<>();
		
		for(DBEntry<IQuest> entry : actQuest)
		{
		    Runnable callback = () -> dirty.add(entry.getID());
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskCrafting) ((TaskCrafting)task.getValue()).onItemAnvil(event.getEntityPlayer(), event.getItemResult().copy(), callback);
            }
		}
		
		if(dirty.size() > 0) bulkMarkDirtyPlayer(actParty, dirty);
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityKilled(LivingDeathEvent event)
	{
		if(event.getSource() == null || !(event.getSource().getTrueSource() instanceof EntityPlayer) || event.getSource().getTrueSource().world.isRemote || event.isCanceled()) return;
		
		EntityPlayer player = (EntityPlayer)event.getSource().getTrueSource();
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskHunt) ((TaskHunt)task.getValue()).onKilledByPlayer(entry, player, event.getEntityLiving(), event.getSource());
            }
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityTamed(AnimalTameEvent event)
	{
		if(event.getTamer() == null || event.getTamer().world.isRemote || event.isCanceled()) return;
		
		EntityPlayer player = event.getTamer();
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskTame) ((TaskTame)task.getValue()).onAnimalTamed(entry, player, event.getEntityLiving());
            }
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockEvent.BreakEvent event)
	{
		if(event.getPlayer() == null || event.getPlayer().world.isRemote || event.isCanceled()) return;
		
        QuestCache qc = event.getPlayer().getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskBlockBreak) ((TaskBlockBreak)task.getValue()).onBlockBreak(entry, event.getPlayer(), event.getState(), event.getPos());
            }
		}
	}
	
	@SubscribeEvent
    public void onEntityLiving(LivingUpdateEvent event)
    {
        if(!(event.getEntityLiving() instanceof EntityPlayer) || event.getEntityLiving().world.isRemote || event.getEntityLiving().ticksExisted%20 != 0 || QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE)) return;
        
        EntityPlayer player = (EntityPlayer)event.getEntityLiving();
		
		List<EntityPlayer> actParty = getActiveParty(player);
		List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(getSharedQuests(actParty));
		List<Integer> dirty = new ArrayList<>();
		
		for(DBEntry<IQuest> entry : actQuest)
		{
		    Runnable callback = () -> dirty.add(entry.getID());
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof ITaskTickable)
                {
                    ((ITaskTickable)task.getValue()).tickTask(player, callback);
                } else if(task.getValue() instanceof TaskTrigger)
                {
                    ((TaskTrigger)task.getValue()).checkSetup(player, entry);
                }
            }
		}
		
		if(dirty.size() > 0) bulkMarkDirtyPlayer(actParty, dirty);
    }
    
    @SubscribeEvent
    public void onAdvancement(AdvancementEvent event)
    {
        if(event.getEntityPlayer() == null || event.getEntity().world.isRemote) return;
		
        QuestCache qc = event.getEntityPlayer().getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
		if(qc == null) return;
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskAdvancement) ((TaskAdvancement)task.getValue()).onAdvancementGet(entry.getValue(), event.getEntityPlayer(), event.getAdvancement());
            }
		}
    }
    
    @SubscribeEvent
    public void onEntityCreated(EntityJoinWorldEvent event)
    {
        if(!(event.getEntity() instanceof EntityPlayer) || event.getEntity().world.isRemote) return;
        
		PlayerContainerListener.refreshListener((EntityPlayer)event.getEntity());
    }
    
    @SubscribeEvent
    public void onServerTick(ServerTickEvent event)
    {
        if(event.phase != Phase.START || FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() % 60 != 0) return;
        
        AdvListenerManager.INSTANCE.updateAll();
    }
	
	@SubscribeEvent
    public void onPlayerJoin(PlayerLoggedInEvent event)
    {
		if(!event.player.world.isRemote && event.player instanceof EntityPlayerMP)
		{
            NetLootSync.sendSync((EntityPlayerMP)event.player);
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
    
    // === LAZY FUNCTIONS ===
    
    public static List<UUID> getBulkUUIDs(@Nonnull List<EntityPlayer> players)
    {
        List<UUID> list = new ArrayList<>();
        players.forEach((value) -> list.add(QuestingAPI.getQuestingUUID(value)));
        return list;
    }
    
    @Nonnull
    public static List<EntityPlayer> getActiveParty(@Nonnull EntityPlayer player)
    {
        UUID playerID = QuestingAPI.getQuestingUUID(player);
        DBEntry<IParty> party = QuestingAPI.getAPI(ApiReference.PARTY_DB).getParty(playerID);
        if(party == null || player.getServer() == null) return Collections.singletonList(player);
        
        MinecraftServer server = player.getServer();
        List<EntityPlayer> active = new ArrayList<>();
        for(UUID mem : party.getValue().getMembers())
        {
            EntityPlayer pMem = server.getPlayerList().getPlayerByUUID(mem);
            //noinspection ConstantConditions
            if(pMem != null) active.add(pMem);
        }
        
        return active;
    }
    
    @Nonnull
    public static int[] getSharedQuests(@Nonnull List<EntityPlayer> players)
    {
        TreeSet<Integer> active = new TreeSet<>();
        players.forEach((p) -> {
            QuestCache qc = p.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
            if(qc != null) for(int value : qc.getActiveQuests()) active.add(value);
        });
        
        int[] merged = new int[active.size()];
        int i = 0;
        for(int value : active) merged[i++] = value;
        return merged;
    }
	
	public static void bulkMarkDirtyPlayer(@Nonnull List<EntityPlayer> players, @Nonnull List<Integer> questID)
    {
        if(players.size() <= 0) return;
        players.forEach((value) -> {
            QuestCache qc = value.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
            if(qc != null) questID.forEach(qc::markQuestDirty);
        });
    }
	
	public static void bulkMarkDirtyUUID(@Nonnull List<UUID> uuids, @Nonnull List<Integer> questID)
    {
        if(uuids.size() <= 0) return;
        final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if(server == null) return;
        
        uuids.forEach((value) -> {
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(value);
            //noinspection ConstantConditions
            if(player == null) return;
            QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
            if(qc != null) questID.forEach(qc::markQuestDirty);
        });
    }
}
