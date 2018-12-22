package bq_standard.rewards;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.PanelLegacyEmbed;
import bq_standard.XPHelper;
import bq_standard.client.gui.rewards.GuiRewardXP;
import bq_standard.rewards.factory.FactoryRewardXP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
		XPHelper.addXP(player, !levels? amount : XPHelper.getLevelXP(amount));
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		amount = json.getInteger("amount");
		levels = json.getBoolean("isLevels");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		json.setInteger("amount", amount);
		json.setBoolean("isLevels", levels);
		return json;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IGuiPanel getRewardGui(IGuiRect rect, IQuest quest)
	{
		return new PanelLegacyEmbed<>(rect, new GuiRewardXP(this, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getRewardEditor(GuiScreen screen, IQuest quest)
	{
		return null;
	}

	@Override
	public IJsonDoc getDocumentation()
	{
		return null;
	}
}
