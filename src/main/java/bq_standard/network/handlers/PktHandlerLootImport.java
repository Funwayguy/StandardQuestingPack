package bq_standard.network.handlers;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.IPacketHandler;
import bq_standard.core.BQ_Standard;
import bq_standard.network.StandardPacketType;
import bq_standard.rewards.loot.LootGroup;
import bq_standard.rewards.loot.LootRegistry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;

public class PktHandlerLootImport implements IPacketHandler
{
	@Override
	public ResourceLocation getRegistryName()
	{
		return StandardPacketType.LOOT_IMPORT.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender)
	{
		if(sender == null || sender.getServer() == null) return;
		
		if(!sender.getServer().getPlayerList().canSendCommands(sender.getGameProfile()))
		{
			BQ_Standard.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to import loot without OP permissions!");
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit loot!"));
			return; // Player is not operator. Do nothing
		}
		
		NBTTagList list = tag.getCompoundTag("data").getTagList("groups", 10);
		
		for(int i = 0; i < list.tagCount(); i++)
		{
			LootGroup group = new LootGroup();
			group.readFromNBT(list.getCompoundTagAt(i), EnumSaveType.CONFIG);
			LootRegistry.INSTANCE.add(LootRegistry.INSTANCE.nextID(), group); // The ID doesn't particularly matter at this point
		}
		
		LootRegistry.INSTANCE.updateClients();
	}
	
	@Override
	public void handleClient(NBTTagCompound tag)
	{
	}
}
