package bq_standard.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.network.IPacketHandler;
import bq_standard.ScoreboardBQ;
import bq_standard.network.StandardPacketType;

public class PktHandlerScoreboard implements IPacketHandler
{
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		ScoreboardBQ.readJson(data.getTagList("data", 10));
	}

	@Override
	public ResourceLocation getRegistryName()
	{
		return StandardPacketType.SCORE_SYNC.GetLocation();
	}
}
