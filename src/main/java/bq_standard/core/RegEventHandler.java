package bq_standard.core;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class RegEventHandler
{
	public static final List<Item> ALL_ITEMS = new ArrayList<Item>();
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void registerModelEvent(ModelRegistryEvent event)
	{
		BQ_Standard.proxy.registerRenderers();
	}
	
	@SubscribeEvent
	public static void registerItemEvent(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().registerAll(ALL_ITEMS.toArray(new Item[0]));
	}
    
    public static void registerItem(Item i, String name)
    {
    	ResourceLocation res = new ResourceLocation(BQ_Standard.MODID + ":" + name);
    	ALL_ITEMS.add(i.setRegistryName(res));
    }
	
	// SETUP ALL THE THINGS
	static {
    	registerItem(BQ_Standard.lootChest, "loot_chest");
	}
}
