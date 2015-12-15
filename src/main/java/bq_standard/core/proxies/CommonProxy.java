package bq_standard.core.proxies;

import cpw.mods.fml.common.FMLCommonHandler;
import bq_standard.client.gui.UpdateNotification;
import bq_standard.rewards.loot.LootRegistry;
import net.minecraftforge.common.MinecraftForge;

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
	}

	public void registerThemes()
	{
	}
}
