package bq_standard.client.gui.editors;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.editors.json.GuiJsonArray;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.IVolatileScreen;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonObject;

public class GuiCraftingEditor extends GuiQuesting implements IVolatileScreen
{
	JsonObject data = new JsonObject();
	
	public GuiCraftingEditor(GuiScreen parent, JsonObject data)
	{
		super(parent, "bq_standard.title.edit_retrieval");
		this.data = data;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.add(new GuiButtonQuesting(1, guiLeft + sizeX/2 - 100, guiTop + sizeY/2 - 20, 200, 20, StatCollector.translateToLocalFormatted("bq_standard.btn.edit_items")));
		this.buttonList.add(new GuiButtonQuesting(2, guiLeft + sizeX/2 - 100, guiTop + sizeY/2 + 00, 200, 20, StatCollector.translateToLocalFormatted("betterquesting.btn.advanced")));
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
		} else if(button.id == 2) // Advanced edit
		{
			mc.displayGuiScreen(new GuiJsonObject(this, data));
		}
	}
}
