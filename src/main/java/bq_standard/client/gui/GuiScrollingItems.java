package bq_standard.client.gui;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiBQScrolling;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.RenderUtils;

public class GuiScrollingItems extends GuiBQScrolling
{
	ArrayList<String> toolTip = null;
	GuiScreen parent;
	ArrayList<ItemEntry> stacks = new ArrayList<ItemEntry>();
	
	public GuiScrollingItems(GuiScreen parent, int width, int height, int top, int left)
	{
		super(parent.mc, width, height, top, top + height, left, 36);
		this.parent = parent;
	}
	
	public void addEntry(BigItemStack stack, String text)
	{
		ItemEntry entry = new ItemEntry(stack, text);
		stacks.add(entry);
	}
	
	public void clearListing()
	{
		stacks.clear();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		toolTip = null;
		super.drawScreen(mx, my, partialTick);
		
		if(toolTip != null && parent instanceof GuiQuesting)
		{
			// Deferred till after rendering all slots to prevent overlap
			((GuiQuesting)parent).DrawTooltip(toolTip, mx, my);
		}
	}
	
	@Override
	protected void drawBackground()
	{
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void drawSlot(int index, int var2, int posY, int var4, Tessellator var5)
	{
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
        int mx = Mouse.getEventX() * parent.width / parent.mc.displayWidth;
        int my = parent.height - Mouse.getEventY() * parent.height / parent.mc.displayHeight - 1;
        
		boolean clipped = false;
		int t = 0;
		int b = 36;
		
		if(posY < top || posY + slotHeight > bottom)
		{
			t = Math.max(0, top - posY);
			b = Math.min(36, 36 - (posY + slotHeight - bottom));
			clipped = t > 2 || b < 34;
		}
		
		int h = (b/2 - t/2);
		
		ItemEntry entry = stacks.get(index);
		
		if(entry == null || entry.stack == null || entry.text == null)
		{
			return;
		}
		
		GL11.glPushMatrix();
		GL11.glScalef(2F, 2F, 2F); // Double stack size because it looks nicer in the listing
		parent.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		int x = left/2;
		int y = posY/2;
		parent.drawTexturedModalRect(x, y + t/2, 0, 48 + t/2, 18, h);
		
		ItemStack tmpStack = entry.stack.getBaseStack();
		
		if(entry.stack.oreDict != null)
		{
			ArrayList<ItemStack> ores = OreDictionary.getOres(entry.stack.oreDict);
			
			if(ores != null && ores.size() > 0)
			{
				tmpStack = ores.get((int)(Minecraft.getSystemTime()/16000)%ores.size()).copy();
				tmpStack.setItemDamage((int)(Minecraft.getSystemTime()/1000)%16);
				tmpStack.setTagCompound(entry.stack.GetTagCompound());
			}
		}
		
		if(!clipped)
		{
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			
			try
			{
				RenderUtils.RenderItemStack(parent.mc, tmpStack, x + 1, y + 1, entry.stack.stackSize > 0? "" : "" + entry.stack.stackSize);
			} catch(Exception e)
			{
				
			}
		}
		GL11.glPopMatrix();
		
		if(!clipped)
		{
			RenderUtils.drawSplitString(parent.mc.fontRenderer, entry.text, left + 40, posY + 4, listWidth - 48, ThemeRegistry.curTheme().textColor().getRGB(), false, 0, 2);
			
			if(mx >= left + 2 && mx < left + 34 && my >= posY + 2 && my < posY + 34)
			{
				toolTip = (ArrayList<String>)tmpStack.getTooltip(parent.mc.thePlayer, parent.mc.gameSettings.advancedItemTooltips);
			}
		}
	}
	
	@Override
	protected void elementClicked(int arg0, boolean arg1)
	{
	}
	
	@Override
	protected int getSize()
	{
		return stacks.size();
	}

    @Override
    protected int getContentHeight()
    {
    	return getSize() * 36;
    }
	
	@Override
	protected boolean isSelected(int arg0)
	{
		return false;
	}
	
	static class ItemEntry
	{
		public BigItemStack stack;
		public String text;
		
		public ItemEntry(BigItemStack stack, String text)
		{
			this.stack = stack;
			this.text = text;
		}
	}
}
