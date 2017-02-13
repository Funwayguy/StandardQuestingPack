package bq_standard.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import bq_standard.core.BQ_Standard;
import bq_standard.network.StandardPacketType;
import bq_standard.rewards.loot.LootGroup;
import bq_standard.rewards.loot.LootRegistry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
		if(sender == null)
		{
			return;
		}
		
		boolean isOP = sender.world.getMinecraftServer().getPlayerList().canSendCommands(sender.getGameProfile());
		
		if(!isOP)
		{
			BQ_Standard.logger.log(Level.WARN, "Player " + sender.getName() + " (UUID:" + QuestingAPI.getQuestingUUID(sender) + ") tried to import loot without OP permissions!");
			sender.sendMessage(new TextComponentString(TextFormatting.RED + "You need to be OP to edit loot!"));
			return; // Player is not operator. Do nothing
		}
		
		JsonObject base = NBTConverter.NBTtoJSON_Compound(tag.getCompoundTag("data"), new JsonObject());
		
		for(JsonElement je : JsonHelper.GetArray(base, "groups"))
		{
			if(je == null || !je.isJsonObject())
			{
				continue;
			}
			
			LootGroup group = new LootGroup();
			group.readFromJson(je.getAsJsonObject());
			LootRegistry.registerGroup(group);
		}
		
		LootRegistry.updateClients();
	}
	
	@Override
	public void handleClient(NBTTagCompound tag)
	{
	}
}
