package bq_standard.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.IPacketHandler;
import bq_standard.core.BQ_Standard;
import bq_standard.network.StandardPacketType;
import bq_standard.rewards.loot.LootRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;

public class PktHandlerLootDatabase implements IPacketHandler
{
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
	    if(sender == null || sender.getServer() == null) return;
	    
		if(!sender.getServer().getPlayerList().canSendCommands(sender.getGameProfile()))
		{
			BQ_Standard.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to edit loot chests without OP permissions!");
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit loot!"));
			return; // Player is not operator. Do nothing
		}
		
		BQ_Standard.logger.log(Level.INFO, "Player " + sender.getName() + " edited loot chests");
		
		LootRegistry.INSTANCE.readFromNBT(data.getCompoundTag("Database"), EnumSaveType.CONFIG);
		LootRegistry.INSTANCE.updateClients();
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		LootRegistry.INSTANCE.readFromNBT(data.getCompoundTag("Database"), EnumSaveType.CONFIG);
	}

	@Override
	public ResourceLocation getRegistryName()
	{
		return StandardPacketType.LOOT_SYNC.GetLocation();
	}
}
