package bq_standard.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.BlockEvent;
import betterquesting.api.ExpansionAPI;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.properties.NativeProps;
import betterquesting.api.quests.tasks.ITask;
import bq_standard.tasks.TaskBlockBreak;
import bq_standard.tasks.TaskCrafting;
import bq_standard.tasks.TaskHunt;
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
		
		for(Entry<TaskCrafting,IQuest> entry : getActiveTasks(TaskCrafting.class, ExpansionAPI.getAPI().getNameCache().getQuestingID(event.player)).entrySet())
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
		
		for(Entry<TaskCrafting,IQuest> entry : getActiveTasks(TaskCrafting.class, ExpansionAPI.getAPI().getNameCache().getQuestingID(event.player)).entrySet())
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
		
		for(Entry<TaskHunt,IQuest> entry : getActiveTasks(TaskHunt.class, ExpansionAPI.getAPI().getNameCache().getQuestingID(player)).entrySet())
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
		
		for(Entry<TaskBlockBreak,IQuest> entry : getActiveTasks(TaskBlockBreak.class, ExpansionAPI.getAPI().getNameCache().getQuestingID(event.getPlayer())).entrySet())
		{
			entry.getKey().onBlockBreak(entry.getValue(), event.getPlayer(), event.block, event.blockMetadata, event.x, event.y, event.z);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T extends ITask> Map<T, IQuest> getActiveTasks(Class<T> cls, UUID uuid)
	{
		List<IQuest> qList = ExpansionAPI.getAPI().getQuestDB().getAllValues();
		Map<T, IQuest> tMap = new HashMap<T, IQuest>();
		
		if(cls == null)
		{
			return tMap;
		}
		
		for(IQuest q : qList)
		{
			if(uuid != null)
			{
				if(q.isComplete(uuid))
				{
					continue;
				} else if(!q.getProperties().getProperty(NativeProps.LOCKED_PROGRESS) && !q.isUnlocked(uuid))
				{
					continue;
				}
			}
			
			for(ITask t : q.getTasks().getAllValues())
			{
				if(cls.isAssignableFrom(t.getClass()))
				{
					if(uuid == null || !t.isComplete(uuid))
					{
						tMap.put((T)t, q);
					}
				}
			}
		}
		
		return tMap;
	}
}
