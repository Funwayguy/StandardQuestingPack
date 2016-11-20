package bq_standard.rewards;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import betterquesting.api.ExpansionAPI;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.rewards.IReward;
import betterquesting.api.utils.JsonHelper;
import bq_standard.AdminExecute;
import bq_standard.client.gui.rewards.GuiRewardCommand;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.factory.FactoryRewardCommand;
import com.google.gson.JsonObject;

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
	public void claimReward(EntityPlayer player, IQuest quest)
	{
		if(player.worldObj.isRemote)
		{
			return;
		}
		
		String tmp = command.replaceAll("VAR_NAME", player.getCommandSenderName());
		tmp = tmp.replaceAll("VAR_UUID", ExpansionAPI.getAPI().getNameCache().getQuestingID(player).toString());
		
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
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		command = JsonHelper.GetString(json, "command", "/say VAR_NAME Claimed a reward");
		hideCmd = JsonHelper.GetBoolean(json, "hideCommand", false);
		viaPlayer = JsonHelper.GetBoolean(json, "viaPlayer", false);
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		json.addProperty("command", command);
		json.addProperty("hideCommand", hideCmd);
		json.addProperty("viaPlayer", viaPlayer);
		return json;
	}
	
	@Override
	public IGuiEmbedded getRewardGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new GuiRewardCommand(this, posX, posY, sizeX, sizeY);
	}
	
	@Override
	public GuiScreen getRewardEditor(GuiScreen screen, IQuest quest)
	{
		return null;
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
