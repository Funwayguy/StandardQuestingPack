package bq_standard.client.gui.rewards;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.RenderUtils;
import bq_standard.rewards.RewardChoice;

public class GuiRewardChoice extends GuiElement implements IGuiEmbedded
{
	private RewardChoice reward;
	private Minecraft mc;
	
	private int posX = 0;
	private int posY = 0;
	private int sizeX = 0;
	private int scroll = 0;
	
	public GuiRewardChoice(RewardChoice reward, int posX, int posY, int sizeX, int sizeY)
	{
		this.mc = Minecraft.getMinecraft();
		this.reward = reward;
		
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		int rowLMax = (sizeX - 40)/18;
		int rowL = Math.min(reward.choices.size(), rowLMax);
		
		if(rowLMax < reward.choices.size())
		{
			scroll = MathHelper.clamp_int(scroll, 0, reward.choices.size() - rowLMax);
			//RenderUtils.DrawFakeButton(screen, posX, posY, 20, 20, "<", screen.isWithin(mx, my, posX, posY, 20, 20, false)? 2 : 1);
			//RenderUtils.DrawFakeButton(screen, posX + 20 + 18*rowL, posY, 20, 20, ">", screen.isWithin(mx, my, posX + 20 + 18*rowL, posY, 20, 20, false)? 2 : 1);
		} else
		{
			scroll = 0;
		}
		
		BigItemStack ttStack = null; // Reset
		
		for(int i = 0; i < rowL; i++)
		{
			BigItemStack stack = reward.choices.get(i + scroll);
			mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			drawTexturedModalRect(posX + (i * 18) + 20, posY + 1, 0, 48, 18, 18);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderUtils.RenderItemStack(mc, stack.getBaseStack(), posX + (i * 18) + 21, posY + 2, stack != null && stack.stackSize > 1? "" + stack.stackSize : "");
			
			if(isWithin(mx, my, posX + (i * 18) + 20, posY + 1, 16, 16))
			{
				ttStack = stack;
			}
		}
		
		mc.fontRenderer.drawString(I18n.format("betterquesting.gui.selection"), posX, posY + 32, getTextColor(), false);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		drawTexturedModalRect(posX + 50, posY + 28, 0, 48, 18, 18);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		int sel = reward.getSelecton(mc.thePlayer.getGameProfile().getId());
		BigItemStack stack = (sel >= 0 && sel < reward.choices.size())? reward.choices.get(sel) : null;
		
		if(stack != null)
		{
			RenderUtils.RenderItemStack(mc, stack.getBaseStack(), posX + 51, posY + 29, stack != null && stack.stackSize > 1? "" + stack.stackSize : "");
			
			if(isWithin(mx, my, posX + 51, posY + 29, 16, 16))
			{
				ttStack = stack;
			}
		}
		
		if(ttStack != null)
		{
			drawTooltip(ttStack.getBaseStack().getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips), mx, my, mc.fontRenderer);
		}
	}
	
	@Override
	public void drawForeground(int mx, int my, float partialTick)
	{
		
	}
	
	@Override
	public void onMouseClick(int mx, int my, int button)
	{
		if(button != 0)
		{
			return;
		}
		
		int rowLMax = (sizeX - 40)/18;
		int rowL = Math.min(reward.choices.size(), rowLMax);
		
		if(isWithin(mx, my, posX + 20, posY + 2, rowL * 18, 18))
		{
			int i = (mx - posX - 20)/18;
			reward.setSelection(mc.thePlayer.getGameProfile().getId(), i + scroll);
		} else if(isWithin(mx, my, posX, posY, 20, 20))
		{
			scroll = MathHelper.clamp_int(scroll - 1, 0, reward.choices.size() - rowLMax);
		} else if(isWithin(mx, my, posX + 20 + 18*rowL, posY, 20, 20))
		{
			scroll = MathHelper.clamp_int(scroll + 1, 0, reward.choices.size() - rowLMax);
		}
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
