package bq_standard.rewards;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import bq_standard.AdminExecute;
import bq_standard.client.gui2.rewards.PanelRewardCommand;
import bq_standard.rewards.factory.FactoryRewardCommand;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RewardCommand implements IReward
{
	public String command = "/say VAR_NAME Claimed a reward";
	public boolean hideCmd = false;
	public boolean viaPlayer = false;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryRewardCommand.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.reward.command";
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, IQuest quest)
	{
		return true;
	}
	
	@Override
	public void claimReward(final EntityPlayer player, IQuest quest)
	{
		if(player.world.isRemote)
		{
			return;
		}
		
		String tmp = command.replaceAll("VAR_NAME", player.getName());
		final String finCom = tmp.replaceAll("VAR_UUID", QuestingAPI.getQuestingUUID(player).toString());
		final MinecraftServer server = player.world.getMinecraftServer();
		
		if(viaPlayer)
		{
			server.addScheduledTask(new Runnable()
			{
				@Override
				public void run()
				{
					server.getCommandManager().executeCommand(new AdminExecute(player), finCom);
				}
			});
		} else
		{
			final RewardCommandSender cmdSender = new RewardCommandSender(player.world, (int)player.posX, (int)player.posY, (int)player.posZ);
			
			server.addScheduledTask(new Runnable()
			{
				@Override
				public void run()
				{
					server.getCommandManager().executeCommand(cmdSender, finCom);
				}
			});
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		command = json.getString("command");
		hideCmd = json.getBoolean("hideCommand");
		viaPlayer = json.getBoolean("viaPlayer");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		json.setString("command", command);
		json.setBoolean("hideCommand", hideCmd);
		json.setBoolean("viaPlayer", viaPlayer);
		return json;
	}
	
	@Override
	public IGuiPanel getRewardGui(IGuiRect rect, IQuest quest)
	{
	    return new PanelRewardCommand(rect, quest, this);
	}
	
	@Override
	public GuiScreen getRewardEditor(GuiScreen screen, IQuest quest)
	{
		return null;
	}
	
	public static class RewardCommandSender extends CommandBlockBaseLogic
	{
		World world;
		BlockPos blockLoc;
		
		public RewardCommandSender(World world, int x, int y, int z)
	    {
	    	blockLoc = new BlockPos(x, y, z);
	    	this.world = world;
	    }

		@Override
		public BlockPos getPosition()
		{
			return blockLoc;
		}

		@Override
		public Vec3d getPositionVector()
		{
			return new Vec3d(blockLoc.getX() + 0.5D, blockLoc.getY() + 0.5D, blockLoc.getZ() + 0.5D);
		}

		@Override
		public World getEntityWorld()
		{
			return world;
		}

		@Override
		public Entity getCommandSenderEntity()
		{
			return null;
		}

		@Override
		public void updateCommand()
		{
		}

		@Override
		public int getCommandBlockType()
		{
			return 0;
		}

		@Override
		public void fillInInfo(ByteBuf p_145757_1_)
		{
		}

	    public String getName()
	    {
	        return "BetterQuesting";
	    }

		@Override
		public MinecraftServer getServer()
		{
			return world.getMinecraftServer();
		}
	}

	@Override
	public IJsonDoc getDocumentation()
	{
		return null;
	}
}
