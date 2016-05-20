package bq_standard.network.handlers;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.network.handlers.PktHandler;
import betterquesting.utils.NBTConverter;
import bq_standard.ScoreboardBQ;

public class PktHandlerScoreboard extends PktHandler
{
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data)
	{
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		ScoreboardBQ.readJson(NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("data"), new JsonObject()));
	}
}
