package bq_standard.handlers;

import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemSmeltedEvent;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.QuestCache;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskBlockBreak;
import bq_standard.tasks.TaskCrafting;
import bq_standard.tasks.TaskHunt;

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
		if(event.getSource() == null || !(event.getSource().getEntity() instanceof EntityPlayer) || event.getSource().getEntity().worldObj.isRemote)
		{
			return;
		}
		
		EntityPlayer player = (EntityPlayer)event.getSource().getEntity();
		
		for(Entry<TaskHunt,IQuest> entry : QuestCache.INSTANCE.getActiveTasks(QuestingAPI.getQuestingUUID(player), TaskHunt.class).entrySet())
		{
			entry.getKey().onKilledByPlayer(entry.getValue(), event.getEntityLiving(), event.getSource());
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
}