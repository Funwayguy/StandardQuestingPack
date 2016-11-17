package bq_standard.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.utils.NBTConverter;
import bq_standard.ScoreboardBQ;
import bq_standard.network.StandardPacketType;
import com.google.gson.JsonObject;

public class PktHandlerScoreboard implements IPacketHandler
{
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		ScoreboardBQ.readJson(NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("data"), new JsonObject()));
	}

	@Override
	public ResourceLocation getRegistryName()
	{
		return StandardPacketType.SCORE_SYNC.GetLocation();
	}
}
