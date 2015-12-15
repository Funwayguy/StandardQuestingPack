package bq_standard.client.gui.editors;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import betterquesting.utils.NBTConverter;
import bq_standard.core.BQ_Standard;
import bq_standard.network.PacketStandard;
import bq_standard.rewards.loot.LootRegistry;

public class GuiLootEditor extends GuiQuesting
{
	JsonObject lastEdit;
	
	public GuiLootEditor(GuiScreen parent)
	{
		super(parent, "");
	}
	
	@Override
	public void initGui()
	{
		if(lastEdit != null)
		{
			LootRegistry.writeToJson(lastEdit);
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", 1);
			tags.setTag("Database", NBTConverter.JSONtoNBT_Object(lastEdit, new NBTTagCompound()));
			BQ_Standard.instance.network.sendToServer(new PacketStandard(tags));
			mc.displayGuiScreen(parent);
			return;
		} else
		{
			lastEdit = new JsonObject();
			LootRegistry.writeToJson(lastEdit);
			mc.displayGuiScreen(new GuiJsonObject(this, lastEdit));
			return;
		}
	}
}
