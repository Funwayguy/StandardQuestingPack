package bq_standard.network;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.logging.log4j.Level;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.NBTConverter;
import bq_standard.client.gui.GuiLootChest;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.loot.LootRegistry;
import bq_standard.tasks.TaskCheckbox;
import com.google.gson.JsonObject;

public class PacketStandard implements IMessage
{
	NBTTagCompound tags = new NBTTagCompound();
	
	public PacketStandard()
	{
	}
	
	public PacketStandard(NBTTagCompound payload)
	{
		tags = payload;
	}
	
	@Override
	public void fromBytes(ByteBuf buf)
	{
		tags = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		if(BQ_Standard.proxy.isClient() && Minecraft.getMinecraft().thePlayer != null)
		{
			tags.setString("Sender", Minecraft.getMinecraft().thePlayer.getUniqueID().toString());
			tags.setInteger("Dimension", Minecraft.getMinecraft().thePlayer.dimension);
		}
		
		ByteBufUtils.writeTag(buf, tags);
	}
	
	public static class HandlerServer implements IMessageHandler<PacketStandard,IMessage>
	{
		@Override
		public IMessage onMessage(PacketStandard message, MessageContext ctx)
		{
			if(message == null || message.tags == null)
			{
				BQ_Standard.logger.log(Level.ERROR, "A critical NPE error occured during while handling a BQ Standard packet server side", new NullPointerException());
				return null;
			}
			
			int ID = !message.tags.hasKey("ID")? -1 : message.tags.getInteger("ID");
			
			if(ID < 0)
			{
				BQ_Standard.logger.log(Level.ERROR, "Recieved a packet server side with an invalid ID", new NullPointerException());
				return null;
			}
			
			EntityPlayer player = null;
			
			if(message.tags.hasKey("Sender"))
			{
				try
				{
					WorldServer world = MinecraftServer.getServer().worldServerForDimension(message.tags.getInteger("Dimension"));
					player = world.getPlayerEntityByUUID(UUID.fromString(message.tags.getString("Sender")));
				} catch(Exception e)
				{
					
				}
			}
			
			if(ID == 1 && player != null)
			{
				if(!MinecraftServer.getServer().getConfigurationManager().canSendCommands(player.getGameProfile()))
				{
					BQ_Standard.logger.log(Level.WARN, "Player " + player.getName() + " (UUID:" + player.getUniqueID() + ") tried to edit loot chests without OP permissions!");
					player.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "You need to be OP to edit loot!"));
					return null; // Player is not operator. Do nothing
				}
				
				BQ_Standard.logger.log(Level.INFO, "Player " + player.getName() + " edited loot chests");
				
				LootRegistry.readFromJson(NBTConverter.NBTtoJSON_Compound(message.tags.getCompoundTag("Database"), new JsonObject()));
				LootRegistry.updateClients();
				return null;
			} else if(ID == 2 && player != null)
			{
				int qId = !message.tags.hasKey("qId")? -1 : message.tags.getInteger("qId");
				int tId = qId < 0 && !message.tags.hasKey("tId")? -1 : message.tags.getInteger("tId");
				
				if(qId >= 0 && tId >= 0)
				{
					try
					{
						TaskBase task = QuestDatabase.getQuestByID(qId).tasks.get(tId);
						
						if(task instanceof TaskCheckbox)
						{
							task.setCompletion(player.getUniqueID(), true);
						}
					} catch(Exception e)
					{
						return null;
					}
				}
			}
			
			return null;
		}
	}
	
	public static class HandlerClient implements IMessageHandler<PacketStandard,IMessage>
	{
		@Override
		public IMessage onMessage(PacketStandard message, MessageContext ctx)
		{
			if(message == null || message.tags == null)
			{
				BQ_Standard.logger.log(Level.ERROR, "A critical NPE error occured during while handling a BQ Standard packet client side", new NullPointerException());
				return null;
			}
			
			int ID = !message.tags.hasKey("ID")? -1 : message.tags.getInteger("ID");
			
			if(ID < 0)
			{
				BQ_Standard.logger.log(Level.ERROR, "Recieved a packet client side with an invalid ID", new NullPointerException());
				return null;
			}
			
			if(ID == 0)
			{
				String title = message.tags.getString("title");
				ArrayList<BigItemStack> rewards = new ArrayList<BigItemStack>();
				
				NBTTagList list = message.tags.getTagList("rewards", 10);
				
				for(int i = 0; i < list.tagCount(); i++)
				{
					BigItemStack stack = BigItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
					
					if(stack != null)
					{
						rewards.add(stack);
					}
				}
				
				Minecraft.getMinecraft().displayGuiScreen(new GuiLootChest(rewards, title));
			} else if(ID == 1)
			{
				LootRegistry.readFromJson(NBTConverter.NBTtoJSON_Compound(message.tags.getCompoundTag("Database"), new JsonObject()));
				return null;
			}
			
			return null;
		}
	}
}
