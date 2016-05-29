package bq_standard.client.gui;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fluids.FluidStack;
import betterquesting.client.gui.misc.GuiBQScrolling;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.utils.RenderUtils;

public class GuiScrollingFluids extends GuiBQScrolling
{
	GuiScreen parent;
	ArrayList<FluidEntry> stacks = new ArrayList<FluidEntry>();
	
	public GuiScrollingFluids(GuiScreen parent, int width, int height, int top, int left)
	{
		super(parent.mc, width, height, top, top + height, left, 36);
		this.parent = parent;
	}
	
	public void addEntry(FluidStack stack, String text)
	{
		FluidEntry entry = new FluidEntry(stack, text);
		stacks.add(entry);
	}
	
	public void clearListing()
	{
		stacks.clear();
	}
	
	@Override
	protected void drawBackground()
	{
	}
	
	@Override
	protected void drawSlot(int index, int var2, int posY, int var4, Tessellator var5)
	{
		GlStateManager.color(1F, 1F, 1F, 1F);
        
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
		
		FluidEntry entry = stacks.get(index);
		
		if(entry == null || entry.stack == null || entry.text == null)
		{
			return;
		}
		
		GlStateManager.pushMatrix();
		GlStateManager.scale(2F, 2F, 2F); // Double stack size because it looks nicer in the listing
		parent.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		int x = left/2;
		int y = posY/2;
		parent.drawTexturedModalRect(x, y + t/2, 0, 48 + t/2, 18, h);
		
		if(!clipped)
		{
			GlStateManager.enableDepth();
			parent.mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			
			try
			{
				parent.mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				TextureAtlasSprite fluidTx = parent.mc.getTextureMapBlocks().getAtlasSprite(entry.stack.getFluid().getStill().toString());
				fluidTx = fluidTx != null? fluidTx : parent.mc.getTextureMapBlocks().getAtlasSprite("missingno");
				parent.drawTexturedModalRect(x + 1, y + 1, fluidTx, 16, 16);
			} catch(Exception e){}
		}
		GlStateManager.popMatrix();
		
		if(!clipped)
		{
			RenderUtils.drawSplitString(parent.mc.fontRendererObj, entry.text, left + 40, posY + 4, listWidth - 48, ThemeRegistry.curTheme().textColor().getRGB(), false, 0, 2);
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
	
	static class FluidEntry
	{
		public FluidStack stack;
		public String text;
		
		public FluidEntry(FluidStack stack, String text)
		{
			this.stack = stack;
			this.text = text;
		}
	}
}
