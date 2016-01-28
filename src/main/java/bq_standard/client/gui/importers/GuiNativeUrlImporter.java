package bq_standard.client.gui.importers;

import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.utils.RenderUtils;
import bq_standard.importers.NativeUrlImporter;

public class GuiNativeUrlImporter extends GuiEmbedded
{
	GuiTextField textUrl;
	GuiButtonQuesting btn;
	public int lastResult = -1;
	
	public GuiNativeUrlImporter(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		btn = new GuiButtonQuesting(0, posX + sizeX/2 - 50, posY + sizeY - 20, 100, 20, I18n.format("betterquesting.btn.import"));
		int tmp = sizeX - 32;
		textUrl = new GuiTextField(screen.mc.fontRenderer, posX + sizeX/2 - tmp/2, posY + sizeY - 50, tmp, 20);
		textUrl.setMaxStringLength(Integer.MAX_VALUE);
		textUrl.setText("http://");
	}
	
	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		RenderUtils.drawSplitString(this.screen.mc.fontRenderer, I18n.format("bq_standard.importer.nat_url.desc"), this.posX + 8, this.posY, this.sizeX - 16, ThemeRegistry.curTheme().textColor().getRGB(), false);
		btn.drawButton(screen.mc, mx, my);
		textUrl.drawTextBox();
	}
	
	@Override
	public void mouseClick(int mx, int my, int button)
	{
		textUrl.mouseClicked(mx, my, button);
		
		if(button == 0 && btn.mousePressed(screen.mc, mx, my))
		{
			NativeUrlImporter.startImport(textUrl.getText());
		}
	}
	
	public void keyTyped(char character, int keyCode)
	{
		textUrl.textboxKeyTyped(character, keyCode);
	}
}
