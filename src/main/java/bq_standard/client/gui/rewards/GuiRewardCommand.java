package bq_standard.client.gui.rewards;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.utils.RenderUtils;
import bq_standard.rewards.RewardCommand;

public class GuiRewardCommand extends GuiElement implements IGuiEmbedded
{
	private RewardCommand reward;
	private Minecraft mc;
	
	private int posX = 0;
	private int posY = 0;
	private int sizeX = 0;
	private int sizeY = 0;
	
	public GuiRewardCommand(RewardCommand reward, int posX, int posY, int sizeX, int sizeY)
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
		String txt1 = I18n.format("advMode.command");
		String txt2 = EnumChatFormatting.ITALIC + (reward.hideCmd? "[HIDDEN]" : reward.command);
		
		mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		IIcon icon = Blocks.command_block.getIcon(0, 0);
		
		GL11.glPushMatrix();
		GL11.glScalef(2F, 2F, 1F);
		RenderUtils.itemRender.renderIcon(posX/2, (posY + sizeY/2 - 16)/2, icon, 16, 16);
		GL11.glPopMatrix();
		
		mc.fontRenderer.drawString(txt1, posX + 40, posY + sizeY/2 - 16, getTextColor());
		mc.fontRenderer.drawString(mc.fontRenderer.trimStringToWidth(txt2, sizeX - (32 + 8)), posX + 40, posY + sizeY/2, getTextColor());
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
