package bq_standard.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.utils.NBTConverter;
import bq_standard.core.BQ_Standard;
import bq_standard.network.StandardPacketType;
import bq_standard.rewards.loot.LootRegistry;
import com.google.gson.JsonObject;

public class PktHandlerLootDatabase implements IPacketHandler
{
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
		if(!MinecraftServer.getServer().getConfigurationManager().func_152596_g(sender.getGameProfile()))
		{
			BQ_Standard.logger.log(Level.WARN, "Player " + sender.getCommandSenderName() + " (UUID:" + sender.getUniqueID() + ") tried to edit loot chests without OP permissions!");
			sender.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "You need to be OP to edit loot!"));
			return; // Player is not operator. Do nothing
		}
		
		BQ_Standard.logger.log(Level.INFO, "Player " + sender.getCommandSenderName() + " edited loot chests");
		
		LootRegistry.readFromJson(NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Database"), new JsonObject()));
		LootRegistry.updateClients();
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		LootRegistry.readFromJson(NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Database"), new JsonObject()));
	}

	@Override
	public ResourceLocation getRegistryName()
	{
		return StandardPacketType.LOOT_SYNC.GetLocation();
	}
}
