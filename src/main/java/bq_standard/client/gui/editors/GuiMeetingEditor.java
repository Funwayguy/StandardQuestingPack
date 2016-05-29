package bq_standard.client.gui.editors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.editors.json.GuiJsonEntitySelection;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.IVolatileScreen;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;

public class GuiMeetingEditor extends GuiQuesting implements IVolatileScreen
{
	String idName = "Villager";
	JsonObject data;
	JsonObject lastEdit = null;
	Entity entity;
	
	public GuiMeetingEditor(GuiScreen parent, JsonObject data)
	{
		super(parent, "bq_standard.title.edit_meeting");
		this.data = data;
		idName = JsonHelper.GetString(data, "target", "Villager");
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		if(lastEdit != null)
		{
			data.addProperty("target", JsonHelper.GetString(lastEdit, "id", "Villager"));
			data.add("targetNBT", lastEdit);
			
			lastEdit = null;
		}
		
		entity = EntityList.createEntityByName(JsonHelper.GetString(data, "target", "Villager"), mc.theWorld);
		
		if(entity == null)
		{
			entity = new EntityVillager(mc.theWorld);
			data.addProperty("target", "Villager");
			data.add("targetNBT", new JsonObject());
		} else
		{
			entity.readFromNBT(NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(data, "targetNBT"), new NBTTagCompound()));
		}
		
		this.buttonList.add(new GuiButtonQuesting(buttonList.size(), guiLeft + sizeX/2 - 100, guiTop + sizeY/2 + 20, 200, 20, I18n.format("bq_standard.btn.select_mob")));
		this.buttonList.add(new GuiButtonQuesting(buttonList.size(), guiLeft + sizeX/2 - 100, guiTop + sizeY/2 + 40, 200, 20, I18n.format("betterquesting.btn.advanced")));
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(entity != null)
		{
			GlStateManager.pushMatrix();
			
			GlStateManager.color(1F, 1F, 1F, 1F);
			
			float angle = ((float)Minecraft.getSystemTime()%30000F)/30000F * 360F;
			float scale = 64F;
			
			if(entity.height * scale > (sizeY/2 - 52))
			{
				scale = (sizeY/2 - 52)/entity.height;
			}
			
			if(entity.width * scale > sizeX)
			{
				scale = sizeX/entity.width;
			}
			
			try
			{
				RenderUtils.RenderEntity(guiLeft + sizeX/2, guiTop + sizeY/4 + MathHelper.ceiling_float_int(entity.height/2F*scale) + 16, (int)scale, angle, 0F, entity);
			} catch(Exception e)
			{
			}
			
			GlStateManager.popMatrix();
		}
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			if(entity != null)
			{
				NBTTagCompound eTags = new NBTTagCompound();
				entity.writeToNBTOptional(eTags);
				lastEdit = NBTConverter.NBTtoJSON_Compound(eTags, new JsonObject());
				mc.displayGuiScreen(new GuiJsonEntitySelection(this, lastEdit));
			}
		} else if(button.id == 2)
		{
			mc.displayGuiScreen(new GuiJsonObject(this, data));
		}
	}
}
