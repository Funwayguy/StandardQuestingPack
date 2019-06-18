package bq_standard.rewards;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import bq_standard.AdminExecute;
import bq_standard.client.gui.rewards.PanelRewardCommand;
import bq_standard.rewards.factory.FactoryRewardCommand;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.FunctionObject;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class RewardCommand implements IReward
{
	public String command = "#Script Comment\nsay Running reward script...\nsay @s Claimed a reward";
	public String title = "bq_standard.reward.command";
	public String desc = "Run a command script";
	public boolean viaPlayer = false;
	public boolean hideIcon = true;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryRewardCommand.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return title;
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, IQuest quest)
	{
		return true;
	}
	
    @Override
	@SuppressWarnings("ConstantConditions")
	public void claimReward(final EntityPlayer player, IQuest quest)
	{
		if(player.world.isRemote)
		{
			return;
		}
		
		// NOTE: These replacements are only kept for legacy reasons. Entity selectors are much more suitable and more powerful
		String tmp = command.replaceAll("VAR_NAME", player.getName());
		String finCom = tmp.replaceAll("VAR_UUID", QuestingAPI.getQuestingUUID(player).toString());
		String[] comAry = finCom.split("\n");
		
		// New functions don't support preceeding forward slash so we remove them on legacy commands
		for(int i = 0; i < comAry.length; i++) if(comAry[i].startsWith("/")) comAry[i] = comAry[i].replaceFirst("/", "");
		
		MinecraftServer server = player.world.getMinecraftServer();
        FunctionObject func = FunctionObject.create(server.getFunctionManager(), Arrays.asList(comAry));
        ICommandSender sender = viaPlayer ? new AdminExecute(player) : new RewardCommandSender(player);
        
        server.getFunctionManager().execute(func, sender);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json)
	{
		command = json.getString("command");
		title = json.hasKey("title", 8) ? json.getString("title") : "bq_standard.reward.command";
		desc = json.hasKey("description", 8) ? json.getString("description") : "Run a command script";
		viaPlayer = json.getBoolean("viaPlayer");
		hideIcon = !json.hasKey("hideBlockIcon", 1) || json.getBoolean("hideBlockIcon");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json)
	{
		json.setString("command", command);
		json.setString("title", title);
		json.setString("description", desc);
		json.setBoolean("viaPlayer", viaPlayer);
		json.setBoolean("hideBlockIcon", hideIcon);
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
		private final Entity entity;
		
		private RewardCommandSender(@Nonnull Entity entity)
	    {
	    	this.entity = entity;
	    }
     
	    @Nonnull
		@Override
		public BlockPos getPosition()
		{
			return entity.getPosition();
		}
  
		@Nonnull
		@Override
		public Vec3d getPositionVector()
		{
			return entity.getPositionVector();
		}
		
		@Nonnull
		@Override
		public World getEntityWorld()
		{
			return entity.getEntityWorld();
		}
  
		@Override
		public Entity getCommandSenderEntity()
		{
			return entity;
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
		public void fillInInfo(@Nonnull ByteBuf p_145757_1_)
		{
		}
		
		@Nonnull
		@Override
	    public String getName()
	    {
	        return "BetterQuesting";
	    }
     
		@Override
		public MinecraftServer getServer()
		{
			return entity.getServer();
		}
	}
}
