package bq_standard.handlers;

import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.QuestCache;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskBlockBreak;
import bq_standard.tasks.TaskCrafting;
import bq_standard.tasks.TaskHunt;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;

public class EventHandler
{
	@SubscribeEvent
	public void onItemCrafted(ItemCraftedEvent event)
	{
		if(event.player == null || event.player.worldObj.isRemote)
		{
			return;
		}
		
		ItemStack actStack = event.crafting.copy();
		
		if(event.craftMatrix instanceof InventoryCrafting)
		{
			actStack = CraftingManager.getInstance().findMatchingRecipe((InventoryCrafting)event.craftMatrix, event.player.worldObj);
		}
		
		for(Entry<TaskCrafting,IQuest> entry : QuestCache.INSTANCE.getActiveTasks(QuestingAPI.getQuestingUUID(event.player), TaskCrafting.class).entrySet())
		{
			entry.getKey().onItemCrafted(entry.getValue(), event.player, actStack);
		}
	}
	
	@SubscribeEvent
	public void onItemSmelted(ItemSmeltedEvent event)
	{
		if(event.player == null || event.player.worldObj.isRemote)
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
		if(event.source == null || !(event.source.getEntity() instanceof EntityPlayer) || event.source.getEntity().worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer)event.source.getEntity();
		
		for(Entry<TaskHunt,IQuest> entry : QuestCache.INSTANCE.getActiveTasks(QuestingAPI.getQuestingUUID(player), TaskHunt.class).entrySet())
		{
			entry.getKey().onKilledByPlayer(entry.getValue(), event.entityLiving, event.source);;
		}
	}
	
	@SubscribeEvent
	public void onBlockBreak(BlockEvent.BreakEvent event)
	{
		if(event.getPlayer() == null || event.getPlayer().worldObj.isRemote)
		{
			return;
		}
		
		for(Entry<TaskBlockBreak,IQuest> entry : QuestCache.INSTANCE.getActiveTasks(QuestingAPI.getQuestingUUID(event.getPlayer()), TaskBlockBreak.class).entrySet())
		{
			entry.getKey().onBlockBreak(entry.getValue(), event.getPlayer(), event.block, event.blockMetadata, event.x, event.y, event.z);
		}
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
}
