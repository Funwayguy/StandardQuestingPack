package bq_standard.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;
import betterquesting.network.handlers.PktHandler;
import betterquesting.utils.NBTConverter;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.loot.LootRegistry;
import com.google.gson.JsonObject;

public class PktHandlerLootDatabase extends PktHandler
{
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data)
	{
		if(!sender.getServer().getPlayerList().canSendCommands(sender.getGameProfile()))
		{
			BQ_Standard.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + sender.getUniqueID() + ") tried to edit loot chests without OP permissions!");
			sender.addChatComponentMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit loot!"));
			return; // Player is not operator. Do nothing
		}
		
		BQ_Standard.logger.log(Level.INFO, "Player " + sender.getName() + " edited loot chests");
		
		LootRegistry.readFromJson(NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Database"), new JsonObject()));
		LootRegistry.updateClients();
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		LootRegistry.readFromJson(NBTConverter.NBTtoJSON_Compound(data.getCompoundTag("Database"), new JsonObject()));
	}
}
