package bq_standard.client.gui.editors;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.controls.GuiNumberField;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumSaveType;
import bq_standard.client.gui.editors.callback.JsonSaveLoadCallback;
import bq_standard.tasks.TaskScoreboard;
import bq_standard.tasks.TaskScoreboard.ScoreOperation;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;

import java.io.IOException;

public class GuiScoreEditor extends GuiScreenThemed implements IVolatileScreen
{
	private final TaskScoreboard task;
	private final NBTTagCompound data;
	
	private GuiTextField txtField;
	private GuiNumberField numField;
	private ScoreOperation operation = ScoreOperation.MORE_OR_EQUAL;
	
	public GuiScoreEditor(GuiScreen parent, TaskScoreboard task)
	{
		super(parent, "bq_standard.title.edit_hunt");
		this.task = task;
		this.data = task.writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG);
		operation = ScoreOperation.valueOf(data.hasKey("operation", 8) ? data.getString("operation") : "MORE_OR_EQUAL");
		operation = operation != null? operation : ScoreOperation.MORE_OR_EQUAL;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		txtField = new GuiTextField(0, mc.fontRenderer, guiLeft + sizeX/2 - 99, guiTop + sizeY/2 - 19, 198, 18);
		txtField.setText(data.getString("scoreName"));
		numField = new GuiNumberField(mc.fontRenderer, guiLeft + sizeX/2 + 1, guiTop + sizeY/2 + 1, 98, 18);
		numField.setText("" + data.getInteger("target"));
		this.buttonList.add(new GuiButtonThemed(buttonList.size(), guiLeft + sizeX/2 - 100, guiTop + sizeY/2, 100, 20, operation.GetText()));
		this.buttonList.add(new GuiButtonThemed(buttonList.size(), guiLeft + sizeX/2 - 100, guiTop + sizeY/2 + 20, 200, 20, I18n.format("betterquesting.btn.advanced")));
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		mc.fontRenderer.drawString(I18n.format("betterquesting.gui.name"), guiLeft + sizeX/2 - 100, guiTop + sizeY/2 - 32, getTextColor());
		numField.drawTextBox();
		txtField.drawTextBox();
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 0)
		{
			task.readFromNBT(data, EnumSaveType.CONFIG);
		} else if(button.id == 1)
		{
			int i = operation.ordinal();
			operation = ScoreOperation.values()[(i + 1)%ScoreOperation.values().length];
			button.displayString = operation.GetText();
			data.setString("operation", operation.name());
		} else if(button.id == 2)
		{
			//mc.displayGuiScreen(new GuiJsonObject(this, data, null));
			QuestingAPI.getAPI(ApiReference.GUI_HELPER).openJsonEditor(this, new JsonSaveLoadCallback<NBTTagCompound>(task), data, task.getDocumentation());
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
		data.setInteger("target", numField.getNumber().intValue());
		
		txtField.textboxKeyTyped(character, keyCode);
		data.setString("scoreName", txtField.getText());
    }
}
