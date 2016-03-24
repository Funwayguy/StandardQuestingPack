package bq_standard.client.gui.editors;

import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.translation.I18n;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiNumberField;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.utils.JsonHelper;
import bq_standard.tasks.TaskScoreboard.ScoreOperation;
import com.google.gson.JsonObject;

public class GuiScoreEditor extends GuiQuesting
{
	GuiTextField txtField;
	GuiNumberField numField;
	ScoreOperation operation = ScoreOperation.MORE_OR_EQUAL;
	JsonObject data;
	
	public GuiScoreEditor(GuiScreen parent, JsonObject data)
	{
		super(parent, "bq_standard.title.edit_hunt");
		this.data = data;
		operation = ScoreOperation.valueOf(JsonHelper.GetString(data, "operation", "MORE_OR_EQUAL").toUpperCase());
		operation = operation != null? operation : ScoreOperation.MORE_OR_EQUAL;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		txtField = new GuiTextField(0, mc.fontRendererObj, guiLeft + sizeX/2 - 99, guiTop + sizeY/2 - 19, 198, 18);
		txtField.setText(JsonHelper.GetString(data, "scoreName", "Score"));
		numField = new GuiNumberField(mc.fontRendererObj, guiLeft + sizeX/2 + 1, guiTop + sizeY/2 + 1, 98, 18);
		numField.setText("" + JsonHelper.GetNumber(data, "target", 1).intValue());
		this.buttonList.add(new GuiButtonQuesting(buttonList.size(), guiLeft + sizeX/2 - 100, guiTop + sizeY/2, 100, 20, operation.GetText()));
		this.buttonList.add(new GuiButtonQuesting(buttonList.size(), guiLeft + sizeX/2 - 100, guiTop + sizeY/2 + 20, 200, 20, I18n.translateToLocal("betterquesting.btn.advanced")));
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		mc.fontRendererObj.drawString(I18n.translateToLocal("betterquesting.gui.name"), guiLeft + sizeX/2 - 100, guiTop + sizeY/2 - 32, ThemeRegistry.curTheme().textColor().getRGB());
		numField.drawTextBox();
		txtField.drawTextBox();
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			int i = operation.ordinal();
			operation = ScoreOperation.values()[(i + 1)%ScoreOperation.values().length];
			button.displayString = operation.GetText();
			data.addProperty("operation", operation.name());
		} else if(button.id == 2)
		{
			mc.displayGuiScreen(new GuiJsonObject(this, data));
		}
	}
	
    /**
     * Called when the mouse is clicked.
     */
	@Override
    protected void mouseClicked(int mx, int my, int click) throws IOException
    {
		super.mouseClicked(mx, my, click);
		
		numField.mouseClicked(mx, my, click);
		txtField.mouseClicked(mx, my, click);
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int keyCode) throws IOException
    {
        super.keyTyped(character, keyCode);
        
        numField.textboxKeyTyped(character, keyCode);
		data.addProperty("target", numField.getNumber().intValue());
		
		txtField.textboxKeyTyped(character, keyCode);
		data.addProperty("scoreName", txtField.getText());
    }
}
