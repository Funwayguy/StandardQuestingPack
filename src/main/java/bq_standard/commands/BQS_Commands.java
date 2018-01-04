package bq_standard.commands;

import java.io.File;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import bq_standard.rewards.loot.LootGroup;
import bq_standard.rewards.loot.LootRegistry;
import com.google.gson.JsonObject;

public class BQS_Commands extends CommandBase
{
	@Override
	public String getName()
	{
		return "bqs_loot";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/bq_loot default [save|load], /bq_loot delete [all|<loot_index>]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if(args.length != 2)
		{
			throw new WrongUsageException(getUsage(sender));
		}
		
		if(args[0].equalsIgnoreCase("default"))
		{
			if(args[1].equalsIgnoreCase("save"))
			{
				NBTTagCompound jsonQ = new NBTTagCompound();
				LootRegistry.writeToJson(jsonQ);
				JsonHelper.WriteToFile(new File(server.getFile("config/betterquesting/"), "DefaultLoot.json"), NBTConverter.NBTtoJSON_Compound(jsonQ, new JsonObject(), true));
				sender.sendMessage(new TextComponentString("Loot database set as global default"));
			} else if(args[1].equalsIgnoreCase("load"))
			{
		    	File f1 = new File("config/betterquesting/DefaultLoot.json");
				NBTTagCompound j1 = new NBTTagCompound();
				
				if(f1.exists())
				{
					j1 = NBTConverter.JSONtoNBT_Object(JsonHelper.ReadFromFile(f1), new NBTTagCompound(), true);
					LootRegistry.readFromJson(j1);
					LootRegistry.updateClients();
					sender.sendMessage(new TextComponentString("Reloaded default loot database"));
				} else
				{
					sender.sendMessage(new TextComponentString(TextFormatting.RED + "No default loot currently set"));
				}
			} else
			{
				throw new WrongUsageException(getUsage(sender));
			}
		} else if(args[0].equalsIgnoreCase("delete"))
		{
			if(args[1].equalsIgnoreCase("all"))
			{
				LootRegistry.lootGroups.clear();
				LootRegistry.updateClients();
				sender.sendMessage(new TextComponentString("Deleted all loot groups"));
			} else
			{
				try
				{
					int idx = Integer.parseInt(args[1]);
					LootGroup group = LootRegistry.lootGroups.remove(idx);
					LootRegistry.updateClients();
					sender.sendMessage(new TextComponentString("Deleted loot group '" + I18n.format(group.name) + "'"));
				} catch(Exception e)
				{
					throw new WrongUsageException(getUsage(sender));
				}
			}
		} else
		{
			throw new WrongUsageException(getUsage(sender));
		}
	}
}
