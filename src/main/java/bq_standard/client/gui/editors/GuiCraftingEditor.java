package bq_standard.client.gui.editors;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.utils.JsonHelper;
import betterquesting.client.gui.editors.json.GuiJsonArray;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import com.google.gson.JsonObject;

public class GuiCraftingEditor extends GuiScreenThemed implements IVolatileScreen
{
	JsonObject data = new JsonObject();
	
	public GuiCraftingEditor(GuiScreen parent, JsonObject data)
	{
		super(parent, "bq_standard.title.edit_crafting");
		this.data = data;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		this.buttonList.add(new GuiButtonThemed(1, guiLeft + sizeX/2 - 100, guiTop + sizeY/2 - 20, 200, 20, StatCollector.translateToLocalFormatted("bq_standard.btn.edit_items")));
		this.buttonList.add(new GuiButtonThemed(2, guiLeft + sizeX/2 - 100, guiTop + sizeY/2 + 00, 200, 20, StatCollector.translateToLocalFormatted("betterquesting.btn.advanced")));
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
			mc.displayGuiScreen(new GuiJsonArray(this, JsonHelper.GetArray(data, "requiredItems"), null));
		} else if(button.id == 2) // Advanced edit
		{
			mc.displayGuiScreen(new GuiJsonObject(this, data, null));
		}
	}
}
