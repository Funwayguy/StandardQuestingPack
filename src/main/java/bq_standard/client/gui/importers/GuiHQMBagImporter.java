package bq_standard.client.gui.importers;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.utils.RenderUtils;
import bq_standard.importers.hqm.HQMBagImporter;

public class GuiHQMBagImporter extends GuiElement implements IGuiEmbedded
{
	private int posX = 0;
	private int posY = 0;
	private int sizeX = 0;
	
	private GuiScreen screen;
	private GuiButtonThemed btn;
	
	public GuiHQMBagImporter(GuiScreen screen, int posX, int posY, int sizeX, int sizeY)
	{
		btn = new GuiButtonThemed(0, posX + sizeX/2 - 50, posY + sizeY - 20, 100, 20, I18n.format("betterquesting.btn.import"));
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
	}
	
	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		RenderUtils.drawSplitString(this.screen.mc.fontRenderer, I18n.format("bq_standard.importer.hqm_bag.desc"), this.posX + 8, this.posY, this.sizeX - 16, getTextColor(), false);
		btn.drawButton(screen.mc, mx, my);
	}
	
	@Override
	public void onMouseClick(int mx, int my, int button)
	{
		if(button == 0 && btn.mousePressed(screen.mc, mx, my))
		{
			HQMBagImporter.StartImport();
		}
	}

	@Override
	public void drawForeground(int mx, int my, float partialTick)
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
