package bq_standard.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import bq_standard.client.gui.UpdateNotification;
import bq_standard.core.BQ_Standard;
import bq_standard.network.PacketStandard;
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
    	
    	BQ_Standard.instance.network.registerMessage(PacketStandard.HandlerServer.class, PacketStandard.class, 1, Side.SERVER);
	}

	public void registerThemes()
	{
	}

	public void registerRenderers()
	{
	}
}
