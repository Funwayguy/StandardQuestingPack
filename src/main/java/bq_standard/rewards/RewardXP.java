package bq_standard.rewards;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import com.google.gson.JsonObject;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.utils.JsonHelper;
import bq_standard.XPHelper;
import bq_standard.client.gui.rewards.GuiRewardXP;

public class RewardXP extends RewardBase
{
	public int amount = 1;
	public boolean levels = true;
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.reward.xp";
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, NBTTagCompound choiceData)
	{
		return true;
	}
	
	@Override
	public void Claim(EntityPlayer player, NBTTagCompound choiceData)
	{
		XPHelper.AddXP(player, !levels? amount : XPHelper.getLevelXP(amount));
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		amount = JsonHelper.GetNumber(json, "amount", 1).intValue();
		levels = JsonHelper.GetBoolean(json, "isLevels", true);
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		json.addProperty("amount", amount);
		json.addProperty("isLevels", levels);
	}
	
	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiRewardXP(this, screen, posX, posY, sizeX, sizeY);
	}
	
}
