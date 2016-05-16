package bq_standard.client.gui.editors;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.translation.I18n;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.editors.json.GuiJsonArray;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.IVolatileScreen;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonObject;

public class GuiRetrievalEditor extends GuiQuesting implements IVolatileScreen
{
	JsonObject data = new JsonObject();
	
	public GuiRetrievalEditor(GuiScreen parent, JsonObject data)
	{
		super(parent, "bq_standard.title.edit_retrieval");
		this.data = data;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.add(new GuiButtonQuesting(1, guiLeft + sizeX/2 - 100, guiTop + sizeY/2 - 20, 200, 20, I18n.translateToLocalFormatted("bq_standard.btn.edit_items")));
		this.buttonList.add(new GuiButtonQuesting(2, guiLeft + sizeX/2 - 100, guiTop + sizeY/2 + 00, 200, 20, I18n.translateToLocalFormatted("bq_standard.btn.consume", JsonHelper.GetBoolean(data, "consume", true))));
		this.buttonList.add(new GuiButtonQuesting(3, guiLeft + sizeX/2 - 100, guiTop + sizeY/2 + 20, 200, 20, I18n.translateToLocalFormatted("betterquesting.btn.advanced")));
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1) // Item listing
		{
			mc.displayGuiScreen(new GuiJsonArray(this, JsonHelper.GetArray(data, "requiredItems")));
		} else if(button.id == 2) // Toggle consume
		{
			boolean consume = JsonHelper.GetBoolean(data, "consume", true);
			data.addProperty("consume", !consume);
			//button.displayString = StatCollector.translateToLocalFormatted("bq_standard.gui.consume", JsonHelper.GetBoolean(data, "consume", true));
			button.displayString = "Consume: " + !consume;
		} else if(button.id == 3) // Advanced edit
		{
			mc.displayGuiScreen(new GuiJsonObject(this, data));
		}
	}
}
