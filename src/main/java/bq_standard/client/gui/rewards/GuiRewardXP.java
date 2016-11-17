package bq_standard.client.gui.rewards;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.IGuiEmbedded;
import bq_standard.rewards.RewardXP;

public class GuiRewardXP extends GuiElement implements IGuiEmbedded
{
	private RewardXP reward;
	private Minecraft mc;
	
	private int posX = 0;
	private int posY = 0;
	private int sizeX = 0;
	private int sizeY = 0;
	
	public GuiRewardXP(RewardXP reward, int posX, int posY, int sizeX, int sizeY)
	{
		this.mc = Minecraft.getMinecraft();
		this.reward = reward;
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}

	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		String txt1 = I18n.format("bq_standard.gui.experience");
		String txt2 = "" + EnumChatFormatting.BOLD;
		
		if(reward.amount >= 0)
		{
			txt2 += EnumChatFormatting.GREEN + "+" + Math.abs(reward.amount);
		} else
		{
			txt2 += EnumChatFormatting.RED + "-" + Math.abs(reward.amount);
		}
		
		txt2 += reward.levels? "L" : "XP";
		
		GL11.glPushMatrix();
		GL11.glScalef(1.5F, 1.5F, 1F);
		mc.fontRenderer.drawString(txt1, (int)((posX + sizeX/2 - mc.fontRenderer.getStringWidth(txt1)/1.5F)/1.5F), (int)((posY + sizeY/2 - 16)/1.5F), getTextColor(), false);
		mc.fontRenderer.drawString(txt2, (int)((posX + sizeX/2 - mc.fontRenderer.getStringWidth(txt2)/1.5F)/1.5F), (int)((posY + sizeY/2)/1.5F), getTextColor(), false);
		GL11.glPopMatrix();
	}

	@Override
	public void drawForeground(int mx, int my, float partialTick)
	{
	}

	@Override
	public void onMouseClick(int mx, int my, int click)
	{
	}

	@Override
	public void onMouseScroll(int mx, int my, int scroll)
	{
	}

	@Override
	public void onKeyTyped(char c, int keyCode)
	{
	}
}
