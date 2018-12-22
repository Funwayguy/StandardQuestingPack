package bq_standard.client.gui.rewards;

import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.RenderUtils;
import bq_standard.client.gui.GuiScrollingItemsSmall;
import bq_standard.rewards.RewardItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class GuiRewardItem extends GuiElement implements IGuiEmbedded
{
	private Minecraft mc;
	
	private GuiScrollingItemsSmall itemScroll;
	private int posX = 0;
	private int posY = 0;
	private int sizeY = 0;
	
	public GuiRewardItem(RewardItem reward, int posX, int posY, int sizeX, int sizeY)
	{
		this.mc = Minecraft.getMinecraft();
		this.posX = posX;
		this.posY = posY;
		this.sizeY = sizeY;
		
		this.itemScroll = new GuiScrollingItemsSmall(mc, posX + 36, posY, sizeX - 36, sizeY);
		
		for(BigItemStack stack : reward.items)
		{
			this.itemScroll.addItem(new BigItemStack(stack.getBaseStack()), stack.stackSize + " " + stack.getBaseStack().getDisplayName());
		}
	}
	
	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate(posX, posY + sizeY/2 - 16, 0);
		GlStateManager.scale(2F, 2F, 1F);
		GlStateManager.enableDepth();
		RenderUtils.RenderItemStack(mc, new ItemStack(Blocks.CHEST), 0, 0, "");
		GlStateManager.disableDepth();
		GlStateManager.popMatrix();
		itemScroll.drawBackground(mx, my, partialTick);
	}
	
	@Override
	public void drawForeground(int mx, int my, float partialTick)
	{
		itemScroll.drawForeground(mx, my, partialTick);
	}
	
	@Override
	public void onMouseClick(int mx, int my, int button)
	{
		itemScroll.onMouseClick(mx, my, button);
	}
	
	@Override
	public void onMouseRelease(int mx, int my, int click)
	{
		itemScroll.onMouseRelease(mx, my, click);
	}
	
	@Override
	public void onMouseScroll(int mx, int my, int scroll)
	{
		itemScroll.onMouseScroll(mx, my, scroll);
	}
	
	@Override
	public void onKeyTyped(char c, int keyCode)
	{
	}
}
