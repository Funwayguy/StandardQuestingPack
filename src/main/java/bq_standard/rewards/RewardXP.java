package bq_standard.rewards;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.rewards.IReward;
import betterquesting.api.utils.JsonHelper;
import bq_standard.XPHelper;
import bq_standard.client.gui.rewards.GuiRewardXP;
import bq_standard.rewards.factory.FactoryRewardXP;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RewardXP implements IReward
{
	public int amount = 1;
	public boolean levels = true;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryRewardXP.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.reward.xp";
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, IQuest quest)
	{
		return true;
	}
	
	@Override
	public void claimReward(EntityPlayer player, IQuest quest)
	{
		XPHelper.AddXP(player, !levels? amount : XPHelper.getLevelXP(amount));
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		amount = JsonHelper.GetNumber(json, "amount", 1).intValue();
		levels = JsonHelper.GetBoolean(json, "isLevels", true);
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		json.addProperty("amount", amount);
		json.addProperty("isLevels", levels);
		return json;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IGuiEmbedded getRewardGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new GuiRewardXP(this, posX, posY, sizeX, sizeY);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getRewardEditor(GuiScreen screen, IQuest quest)
	{
		return null;
	}
}
