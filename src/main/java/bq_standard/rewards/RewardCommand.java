package bq_standard.rewards;

import io.netty.buffer.ByteBuf;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.utils.JsonHelper;
import bq_standard.AdminExecute;
import bq_standard.client.gui.rewards.GuiRewardCommand;
import bq_standard.core.BQ_Standard;
import com.google.gson.JsonObject;

public class RewardCommand extends RewardBase
{
	public String command = "/say VAR_NAME Claimed a reward";
	public boolean hideCmd = false;
	public boolean viaPlayer = false;
	
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
		
		String tmp = command.replaceAll("VAR_NAME", player.getCommandSenderName());
		tmp = tmp.replaceAll("VAR_UUID", player.getUniqueID().toString());
		
		if(viaPlayer)
		{
			MinecraftServer.getServer().getCommandManager().executeCommand(new AdminExecute(player), tmp);
		} else
		{
			RewardCommandSender cmdSender = new RewardCommandSender(player.worldObj, (int)player.posX, (int)player.posY, (int)player.posZ);
			
			MinecraftServer.getServer().getCommandManager().executeCommand(cmdSender, tmp);
		}
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		command = JsonHelper.GetString(json, "command", "/say VAR_NAME Claimed a reward");
		hideCmd = JsonHelper.GetBoolean(json, "hideCommand", false);
		viaPlayer = JsonHelper.GetBoolean(json, "viaPlayer", false);
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		json.addProperty("command", command);
		json.addProperty("hideCommand", hideCmd);
		json.addProperty("viaPlayer", viaPlayer);
	}
	
	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiRewardCommand(this, screen, posX, posY, sizeX, sizeY);
	}
	
	public static class RewardCommandSender extends CommandBlockLogic
	{
		World world;
		ChunkCoordinates blockLoc;
		
		public RewardCommandSender(World world, int x, int y, int z)
	    {
	    	blockLoc = new ChunkCoordinates(x, y, z);
	    	this.world = world;
	    }
		
		@Override
		public ChunkCoordinates getPlayerCoordinates()
		{
			return blockLoc;
		}
		
		@Override
		public World getEntityWorld()
		{
			return world;
		}
		
		@Override
		public void func_145756_e(){}
		
		@Override
		public int func_145751_f()
		{
			return 0;
		}
		
		@Override
		public void func_145757_a(ByteBuf p_145757_1_){}
	    
	    @Override
	    public String getCommandSenderName()
	    {
	        return BQ_Standard.NAME;
	    }
	}
}
