package bq_standard.client.gui.rewards;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import bq_standard.rewards.RewardScoreboard;

public class GuiRewardScoreboard extends GuiEmbedded
{
	RewardScoreboard reward;
	
	public GuiRewardScoreboard(RewardScoreboard reward, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.reward = reward;
	}

	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		String txt1 = reward.score;
		String txt2 = "" + TextFormatting.BOLD;
		
		if(!reward.relative)
		{
			txt2 += "= " + reward.value;
		} else if(reward.value >= 0)
		{
			txt2 += TextFormatting.GREEN + "+ " + Math.abs(reward.value);
		} else
		{
			txt2 += TextFormatting.RED + "- " + Math.abs(reward.value);
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.scale(1.5F, 1.5F, 1F);
		screen.mc.fontRendererObj.drawString(txt1, (int)((posX + sizeX/2 - screen.mc.fontRendererObj.getStringWidth(txt1)/1.5F)/1.5F), (int)((posY + sizeY/2 - 16)/1.5F), ThemeRegistry.curTheme().textColor().getRGB(), false);
		screen.mc.fontRendererObj.drawString(txt2, (int)((posX + sizeX/2 - screen.mc.fontRendererObj.getStringWidth(txt2)/1.5F)/1.5F), (int)((posY + sizeY/2)/1.5F), ThemeRegistry.curTheme().textColor().getRGB(), false);
		GlStateManager.popMatrix();
	}
}
