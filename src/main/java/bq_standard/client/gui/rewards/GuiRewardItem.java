package bq_standard.client.gui.rewards;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.RenderUtils;
import bq_standard.client.gui.GuiScrollingItemsSmall;
import bq_standard.rewards.RewardItem;

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
		GL11.glPushMatrix();
		GL11.glTranslatef(posX, posY + sizeY/2 - 16, 0);
		GL11.glScalef(2F, 2F, 1F);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderUtils.RenderItemStack(mc, new ItemStack(Blocks.CHEST), 0, 0, "");
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glPopMatrix();
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
