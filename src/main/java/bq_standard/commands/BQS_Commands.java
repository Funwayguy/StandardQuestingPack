package bq_standard.commands;

import java.io.File;
import java.util.ArrayList;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
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
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
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
				JsonHelper.WriteToFile(new File(server.getFile("config/betterquesting/"), "DefaultLoot.json"), jsonQ);
				sender.addChatMessage(new TextComponentString("Loot database set as global default"));
			} else if(args[1].equalsIgnoreCase("load"))
			{
		    	File f1 = new File("config/betterquesting/DefaultLoot.json");
				JsonObject j1 = new JsonObject();
				
				if(f1.exists())
				{
					j1 = JsonHelper.ReadFromFile(f1);
					LootRegistry.readFromJson(j1);
					LootRegistry.updateClients();
					sender.addChatMessage(new TextComponentString("Reloaded default loot database"));
				} else
				{
					sender.addChatMessage(new TextComponentString(TextFormatting.RED + "No default loot currently set"));
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
				sender.addChatMessage(new TextComponentString("Deleted all loot groups"));
			} else
			{
				try
				{
					int idx = Integer.parseInt(args[1]);
					LootGroup group = LootRegistry.lootGroups.remove(idx);
					LootRegistry.updateClients();
					sender.addChatMessage(new TextComponentString("Deleted loot group '" + I18n.format(group.name) + "'"));
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
