package bq_standard.client.gui.importers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.utils.RenderUtils;
import bq_standard.importers.NativeUrlImporter;

public class GuiNativeUrlImporter extends GuiElement implements IGuiEmbedded
{
	private GuiTextField textUrl;
	private Minecraft mc;
	private GuiButtonThemed btn;
	
	private int posX = 0;
	private int posY = 0;
	private int sizeX = 0;
	public int lastResult = -1;
	
	public GuiNativeUrlImporter(int posX, int posY, int sizeX, int sizeY)
	{
		this.mc = Minecraft.getMinecraft();
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
		
		btn = new GuiButtonThemed(0, posX + sizeX/2 - 50, posY + sizeY - 20, 100, 20, I18n.format("betterquesting.btn.import"));
		int tmp = sizeX - 32;
		textUrl = new GuiTextField(mc.fontRenderer, posX + sizeX/2 - tmp/2, posY + sizeY - 50, tmp, 20);
		textUrl.setMaxStringLength(Integer.MAX_VALUE);
		textUrl.setText("http://");
	}
	
	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		RenderUtils.drawSplitString(mc.fontRenderer, I18n.format("bq_standard.importer.nat_url.desc"), this.posX + 8, this.posY, this.sizeX - 16, getTextColor(), false);
		btn.drawButton(mc, mx, my);
		textUrl.drawTextBox();
	}
	
	@Override
	public void onMouseClick(int mx, int my, int button)
	{
		textUrl.mouseClicked(mx, my, button);
		
		if(button == 0 && btn.mousePressed(mc, mx, my))
		{
			NativeUrlImporter.startImport(textUrl.getText());
		}
	}
	
	public void keyTyped(char character, int keyCode)
	{
		textUrl.textboxKeyTyped(character, keyCode);
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
