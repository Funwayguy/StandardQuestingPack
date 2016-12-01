package bq_standard;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class XPHelper
{
	public static void AddXP(EntityPlayer player, int xp)
	{
		int experience = getPlayerXP(player) + xp;
		player.experienceTotal = experience;
		player.experienceLevel = getXPLevel(experience);
		int expForLevel = getLevelXP(player.experienceLevel);
		player.experience = (float)(experience - expForLevel) / (float)player.xpBarCap();
		
		if(player instanceof EntityPlayerMP)
		{
			// Make sure the client isn't being stupid about syncing the experience bars which routinely fail
            ((EntityPlayerMP)player).playerNetServerHandler.sendPacket(new S1FPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));
		}
	}
	
	public static int getPlayerXP(EntityPlayer player)
	{
		return getLevelXP(player.experienceLevel) + (int)(player.experience * player.xpBarCap());
	}
	
	public static int getXPLevel(int xp)
	{
		int i = 0;
		
		while (getLevelXP(i) <= xp)
		{
			i++;
		}
		
		return i - 1;
	}
	
	public static int getLevelXP(int level)
	{
		if(level < 0)
		{
			return 0;
		}
		
		if(level < 16)
		{
			return level * 17;
		} else if(level > 15 && level < 31)
		{
			return (int)(1.5 * Math.pow(level, 2) - 29.5 * level + 360);
		} else
		{
			return (int)(3.5 * Math.pow(level, 2) - 151.5 * level + 2220);
		}
	}
}
