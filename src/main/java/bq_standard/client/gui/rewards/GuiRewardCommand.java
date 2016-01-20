package bq_standard.client.gui.rewards;

import org.lwjgl.opengl.GL11;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.utils.RenderUtils;
import bq_standard.rewards.RewardCommand;

public class GuiRewardCommand extends GuiEmbedded
{
	RewardCommand reward;
	
	public GuiRewardCommand(RewardCommand reward, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.reward = reward;
	}
	
	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		String txt1 = I18n.format("advMode.command");
		String txt2 = ChatFormatting.ITALIC + (reward.hideCmd? "[HIDDEN]" : reward.command);
		
		screen.mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		IIcon icon = Blocks.command_block.getIcon(0, 0);
		
		GL11.glPushMatrix();
		GL11.glScalef(2F, 2F, 1F);
		RenderUtils.itemRender.renderIcon(posX/2, (posY + sizeY/2 - 16)/2, icon, 16, 16);
		GL11.glPopMatrix();
		
		screen.mc.fontRenderer.drawString(txt1, posX + 40, posY + sizeY/2 - 16, ThemeRegistry.curTheme().textColor().getRGB());
		screen.mc.fontRenderer.drawString(screen.mc.fontRenderer.trimStringToWidth(txt2, sizeX - (32 + 8)), posX + 40, posY + sizeY/2, ThemeRegistry.curTheme().textColor().getRGB());
	}
}
