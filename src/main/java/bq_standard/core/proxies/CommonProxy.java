package bq_standard.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import bq_standard.client.gui.UpdateNotification;
import bq_standard.rewards.loot.LootRegistry;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		MinecraftForge.EVENT_BUS.register(new UpdateNotification());
		MinecraftForge.EVENT_BUS.register(new LootRegistry());
	}

	public void registerThemes()
	{
	}

	public void registerRenderers()
	{
	}
}
