package bq_standard.client.gui.editors;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.controls.GuiNumberField;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.RenderUtils;
import bq_standard.client.gui.editors.callback.JsonSaveLoadCallback;
import bq_standard.tasks.TaskHunt;

public class GuiHuntEditor extends GuiScreenThemed implements IVolatileScreen, ICallback<Entity>
{
	private TaskHunt task;
	private GuiNumberField numField;
	private final NBTTagCompound data;
	private Entity entity;
	
	public GuiHuntEditor(GuiScreen parent, TaskHunt task)
	{
		super(parent, "bq_standard.title.edit_hunt");
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
			entity = new EntityZombie(mc.world);
			data.setString("target", "minecraft:zombie");
			data.setTag("targetNBT", new NBTTagCompound());
		} else
		{
			entity.readFromNBT(data.getCompoundTag("targetNBT"));
		}
		
		numField = new GuiNumberField(mc.fontRenderer, guiLeft + sizeX/2 + 1, guiTop + sizeY/2 + 1, 98, 18);
		numField.setText("" + data.getInteger("required"));
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
		
		String txt = I18n.format("bq_standard.gui.amount") + ": ";
		mc.fontRenderer.drawString(txt, guiLeft + sizeX/2 - mc.fontRenderer.getStringWidth(txt), guiTop + sizeY/2 + 6, getTextColor());
		numField.drawTextBox();
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
	
    /**
     * Called when the mouse is clicked.
     */
	@Override
    protected void mouseClicked(int mx, int my, int click) throws IOException
    {
		super.mouseClicked(mx, my, click);
		
		numField.mouseClicked(mx, my, click);
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int keyCode) throws IOException
    {
        super.keyTyped(character, keyCode);
        
        numField.textboxKeyTyped(character, keyCode);
		data.setInteger("required", numField.getNumber().intValue());
    }

	@Override
	public void setValue(Entity value)
	{
		if(value == null || EntityList.getKey(value) == null)
		{
			this.entity = new EntityZombie(mc.world);
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
