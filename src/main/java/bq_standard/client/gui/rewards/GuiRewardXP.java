package bq_standard.client.gui.rewards;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import bq_standard.rewards.RewardXP;

public class GuiRewardXP extends GuiEmbedded
{
	RewardXP reward;
	
	public GuiRewardXP(RewardXP reward, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.reward = reward;
	}

	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		String txt1 = I18n.format("bq_standard.gui.experience");
		String txt2 = "" + TextFormatting.BOLD;
		
		if(reward.amount >= 0)
		{
			txt2 += TextFormatting.GREEN + "+" + Math.abs(reward.amount);
		} else
		{
			txt2 += TextFormatting.RED + "-" + Math.abs(reward.amount);
		}
		
		txt2 += reward.levels? "L" : "XP";
		
		GlStateManager.pushMatrix();
		GlStateManager.scale(1.5F, 1.5F, 1F);
		screen.mc.fontRendererObj.drawString(txt1, (int)((posX + sizeX/2 - screen.mc.fontRendererObj.getStringWidth(txt1)/1.5F)/1.5F), (int)((posY + sizeY/2 - 16)/1.5F), ThemeRegistry.curTheme().textColor().getRGB(), false);
		screen.mc.fontRendererObj.drawString(txt2, (int)((posX + sizeX/2 - screen.mc.fontRendererObj.getStringWidth(txt2)/1.5F)/1.5F), (int)((posY + sizeY/2)/1.5F), ThemeRegistry.curTheme().textColor().getRGB(), false);
		GlStateManager.popMatrix();
	}
}
