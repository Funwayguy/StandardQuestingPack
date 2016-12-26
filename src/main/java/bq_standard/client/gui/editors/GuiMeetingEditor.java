package bq_standard.client.gui.editors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.RenderUtils;
import bq_standard.client.gui.editors.callback.JsonSaveLoadCallback;
import bq_standard.tasks.TaskMeeting;
import com.google.gson.JsonObject;

public class GuiMeetingEditor extends GuiScreenThemed implements IVolatileScreen, ICallback<Entity>
{
	private final TaskMeeting task;
	String idName = "Villager";
	JsonObject data;
	Entity entity;
	
	public GuiMeetingEditor(GuiScreen parent, TaskMeeting task)
	{
		super(parent, "bq_standard.title.edit_meeting");
		this.task = task;
		this.data = task.writeToJson(new JsonObject(), EnumSaveType.CONFIG);
		idName = JsonHelper.GetString(data, "target", "Villager");
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		/*if(lastEdit != null)
		{
			data.addProperty("target", JsonHelper.GetString(lastEdit, "id:8", "Villager"));
			data.add("targetNBT", lastEdit);
			
			lastEdit = null;
		}*/
		
		entity = EntityList.createEntityByName(JsonHelper.GetString(data, "target", "Villager"), mc.theWorld);
		
		if(entity == null)
		{
			entity = new EntityVillager(mc.theWorld);
			data.addProperty("target", "Villager");
			data.add("targetNBT", new JsonObject());
		} else
		{
			entity.readFromNBT(NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(data, "targetNBT"), new NBTTagCompound(), true));
		}
		
		this.buttonList.add(new GuiButtonThemed(buttonList.size(), guiLeft + sizeX/2 - 100, guiTop + sizeY/2 + 20, 200, 20, I18n.format("bq_standard.btn.select_mob")));
		this.buttonList.add(new GuiButtonThemed(buttonList.size(), guiLeft + sizeX/2 - 100, guiTop + sizeY/2 + 40, 200, 20, I18n.format("betterquesting.btn.advanced")));
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(entity != null)
		{
			GL11.glPushMatrix();
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
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
			
			GL11.glPopMatrix();
		}
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 0)
		{
			task.readFromJson(data, EnumSaveType.CONFIG);
		} else if(button.id == 1)
		{
			if(entity != null)
			{
				/*NBTTagCompound eTags = new NBTTagCompound();
				entity.writeToNBTOptional(eTags);
				lastEdit = NBTConverter.NBTtoJSON_Compound(eTags, new JsonObject(), true);*/
				
				//mc.displayGuiScreen(new GuiJsonEntitySelection(this, this, entity));
				QuestingAPI.getAPI(ApiReference.GUI_HELPER).openEntityEditor(this, this, entity);
			}
		} else if(button.id == 2)
		{
			//mc.displayGuiScreen(new GuiJsonObject(this, data, null));
			QuestingAPI.getAPI(ApiReference.GUI_HELPER).openJsonEditor(this, new JsonSaveLoadCallback<JsonObject>(task), data, task.getDocumentation());
		}
	}

	@Override
	public void setValue(Entity value)
	{
		if(value == null)
		{
			this.entity = new EntityVillager(mc.theWorld);
		} else
		{
			this.entity = value;
		}
		
		data.addProperty("target", EntityList.getEntityString(entity));
		NBTTagCompound tTag = new NBTTagCompound();
		entity.writeToNBTOptional(tTag);
		data.add("targetNBT", NBTConverter.NBTtoJSON_Compound(tTag, new JsonObject()));
	}
}
