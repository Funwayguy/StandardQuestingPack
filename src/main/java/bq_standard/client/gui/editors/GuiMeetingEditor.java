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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.RenderUtils;
import bq_standard.client.gui.editors.callback.JsonSaveLoadCallback;
import bq_standard.tasks.TaskMeeting;

public class GuiMeetingEditor extends GuiScreenThemed implements IVolatileScreen, ICallback<Entity>
{
	private final TaskMeeting task;
	private NBTTagCompound data;
	private Entity entity;
	
	public GuiMeetingEditor(GuiScreen parent, TaskMeeting task)
	{
		super(parent, "bq_standard.title.edit_meeting");
		this.task = task;
		this.data = task.writeToNBT(new NBTTagCompound(), EnumSaveType.CONFIG);
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		entity = EntityList.createEntityByIDFromName(new ResourceLocation(data.getString("target")), mc.world);
		
		if(entity == null)
		{
			entity = new EntityVillager(mc.world);
			data.setString("target", "minecraft:villager");
			data.setTag("targetNBT", new NBTTagCompound());
		} else
		{
			entity.readFromNBT(data.getCompoundTag("targetNBT"));
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
				RenderUtils.RenderEntity(guiLeft + sizeX/2, guiTop + sizeY/4 + MathHelper.ceil(entity.height/2F*scale) + 16, (int)scale, angle, 0F, entity);
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
		
		if(button.id == 0)
		{
			task.readFromNBT(data, EnumSaveType.CONFIG);
		} else if(button.id == 1)
		{
			if(entity != null)
			{
				QuestingAPI.getAPI(ApiReference.GUI_HELPER).openEntityEditor(this, this, entity);
			}
		} else if(button.id == 2)
		{
			QuestingAPI.getAPI(ApiReference.GUI_HELPER).openJsonEditor(this, new JsonSaveLoadCallback<>(task), data, task.getDocumentation());
		}
	}

	@Override
	public void setValue(Entity value)
	{
		if(value == null)
		{
			this.entity = new EntityVillager(mc.world);
		} else
		{
			this.entity = value;
		}
		
		data.setString("target", EntityList.getKey(entity).toString());
		NBTTagCompound tTag = new NBTTagCompound();
		entity.writeToNBTOptional(tTag);
		data.setTag("targetNBT", tTag);
	}
}
