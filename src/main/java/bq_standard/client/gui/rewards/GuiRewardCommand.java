package bq_standard.client.gui.rewards;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
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
		String txt2 = TextFormatting.ITALIC + (reward.hideCmd? "[HIDDEN]" : reward.command);
		
		mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		TextureAtlasSprite blockSprite = mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/command_block_front");
		blockSprite = blockSprite != null? blockSprite : mc.getTextureMapBlocks().getAtlasSprite("missingno");
		
		GlStateManager.pushMatrix();
		GlStateManager.scale(2F, 2F, 1F);
		drawTexturedModelRectFromIcon(posX/2, (posY + sizeY/2 - 16)/2, blockSprite, 16, 16);
		GlStateManager.popMatrix();
		
		GlStateManager.color(1F, 1F, 1F, 1F);
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
