package bq_standard.commands;

import java.io.File;
import java.util.ArrayList;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import betterquesting.api.utils.JsonHelper;
import bq_standard.rewards.loot.LootGroup;
import bq_standard.rewards.loot.LootRegistry;
import com.google.gson.JsonObject;

public class BQS_Commands extends CommandBase
{

	@Override
	public String getCommandName()
	{
		return "bqs_loot";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "/bq_loot default [save|load], /bq_loot delete [all|<loot_index>]";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
		if(args.length != 2)
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}
		
		if(args[0].equalsIgnoreCase("default"))
		{
			if(args[1].equalsIgnoreCase("save"))
			{
				JsonObject jsonQ = new JsonObject();
				LootRegistry.writeToJson(jsonQ);
				JsonHelper.WriteToFile(new File(MinecraftServer.getServer().getFile("config/betterquesting/"), "DefaultLoot.json"), jsonQ);
				sender.addChatMessage(new ChatComponentText("Loot database set as global default"));
			} else if(args[1].equalsIgnoreCase("load"))
			{
		    	File f1 = new File("config/betterquesting/DefaultLoot.json");
				JsonObject j1 = new JsonObject();
				
				if(f1.exists())
				{
					j1 = JsonHelper.ReadFromFile(f1);
					LootRegistry.readFromJson(j1);
					LootRegistry.updateClients();
					sender.addChatMessage(new ChatComponentText("Reloaded default loot database"));
				} else
				{
					sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "No default loot currently set"));
				}
			} else
			{
				throw new WrongUsageException(getCommandUsage(sender));
			}
		} else if(args[0].equalsIgnoreCase("delete"))
		{
			if(args[1].equalsIgnoreCase("all"))
			{
				LootRegistry.lootGroups = new ArrayList<LootGroup>();
				LootRegistry.updateClients();
				sender.addChatMessage(new ChatComponentText("Deleted all loot groups"));
			} else
			{
				try
				{
					int idx = Integer.parseInt(args[1]);
					LootGroup group = LootRegistry.lootGroups.remove(idx);
					LootRegistry.updateClients();
					sender.addChatMessage(new ChatComponentText("Deleted loot group '" + I18n.format(group.name) + "'"));
				} catch(Exception e)
				{
					throw new WrongUsageException(getCommandUsage(sender));
				}
			}
		} else
		{
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}
}
