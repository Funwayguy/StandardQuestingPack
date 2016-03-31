package bq_standard;

import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * Elevates the player's privileges to OP level for use in command rewards
 */
public class AdminExecute implements ICommandSender
{
	EntityPlayer player;
	
	public AdminExecute(EntityPlayer player)
	{
		this.player = player;
	}

	@Override
	public String getName()
	{
		return player.getName();
	}

	@Override
	public IChatComponent getDisplayName()
	{
		return player.getDisplayName();
	}

	@Override
	public void addChatMessage(IChatComponent p_145747_1_)
	{
		player.addChatMessage(p_145747_1_);
	}

	@Override
	public boolean canCommandSenderUseCommand(int p_70003_1_, String p_70003_2_)
	{
		return true;
	}

	@Override
	public BlockPos getPosition()
	{
		return player.getPosition();
	}

	@Override
	public World getEntityWorld()
	{
		return player.getEntityWorld();
	}

	@Override
	public Vec3 getPositionVector()
	{
		return player.getPositionVector();
	}

	@Override
	public Entity getCommandSenderEntity()
	{
		return player.getCommandSenderEntity();
	}

	@Override
	public boolean sendCommandFeedback()
	{
		return player.sendCommandFeedback();
	}

	@Override
	public void setCommandStat(Type type, int amount)
	{
		player.setCommandStat(type, amount);
	}
}
