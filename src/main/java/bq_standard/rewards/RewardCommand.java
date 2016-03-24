package bq_standard.rewards;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.utils.JsonHelper;
import bq_standard.client.gui.rewards.GuiRewardCommand;
import bq_standard.core.BQ_Standard;
import com.google.gson.JsonObject;

public class RewardCommand extends RewardBase
{
	public String command = "/say VAR_NAME Claimed a reward";
	public boolean hideCmd = false;
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.reward.command";
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, NBTTagCompound choiceData)
	{
		return true;
	}
	
	@Override
	public void Claim(EntityPlayer player, NBTTagCompound choiceData)
	{
		if(player.worldObj.isRemote)
		{
			return;
		}
		
		String tmp = command.replaceAll("VAR_NAME", player.getName());
		RewardCommandSender cmdSender = new RewardCommandSender(player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
		MinecraftServer server = player.worldObj.getMinecraftServer();
		server.getCommandManager().executeCommand(cmdSender, tmp);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		command = JsonHelper.GetString(json, "command", "/say VAR_NAME Claimed a reward");
		hideCmd = JsonHelper.GetBoolean(json, "hideCommand", false);
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		json.addProperty("command", command);
		json.addProperty("hideCommand", hideCmd);
	}
	
	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiRewardCommand(this, screen, posX, posY, sizeX, sizeY);
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
		public int func_145751_f()
		{
			return 0;
		}

		@Override
		public void func_145757_a(ByteBuf p_145757_1_)
		{
		}

	    /**
	     * Get the name of this object. For players this returns their username
	     */
	    public String getName()
	    {
	        return BQ_Standard.NAME;
	    }

		@Override
		public MinecraftServer getServer()
		{
			return world.getMinecraftServer();
		}
	}
}
