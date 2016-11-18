package bq_standard.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import bq_standard.client.gui.UpdateNotification;
import bq_standard.handlers.EventHandler;
import bq_standard.rewards.loot.LootRegistry;
import cpw.mods.fml.common.FMLCommonHandler;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		FMLCommonHandler.instance().bus().register(new UpdateNotification());
		MinecraftForge.EVENT_BUS.register(new LootRegistry());
		EventHandler eh = new EventHandler();
		MinecraftForge.EVENT_BUS.register(eh);
		FMLCommonHandler.instance().bus().register(eh);
	}

	public void registerThemes()
	{
	}
}
