package bq_standard.client.gui.tasks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.RenderUtils;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskHunt;

public class GuiTaskHunt extends GuiElement implements IGuiEmbedded
{
	private Minecraft mc;
	IQuest quest;
	TaskHunt task;
	Entity target;
	
	private int posX = 0;
	private int posY = 0;
	private int sizeX = 0;
	private int sizeY = 0;
	
	public GuiTaskHunt(TaskHunt task, IQuest quest, int posX, int posY, int sizeX, int sizeY)
	{
		this.mc = Minecraft.getMinecraft();
		this.task = task;
		this.quest = quest;
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}

	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		if(target != null)
		{
			GL11.glPushMatrix();
			
			GL11.glScalef(1F, 1F, 1F);
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
			float angle = ((float)Minecraft.getSystemTime()%30000F)/30000F * 360F;
			float scale = 64F;
			
			if(target.height * scale > (sizeY - 48))
			{
				scale = (sizeY - 48)/target.height;
			}
			
			if(target.width * scale > sizeX)
			{
				scale = sizeX/target.width;
			}
			
			try
			{
				RenderUtils.RenderEntity(posX + sizeX/2, posY + sizeY/2 + MathHelper.ceiling_float_int(target.height/2F*scale) + 8, (int)scale, angle, 0F, target);
			} catch(Exception e)
			{
			}
			
			GL11.glPopMatrix();
		} else
		{
			if(EntityList.NAME_TO_CLASS.containsKey(task.idName))
			{
				try
				{
					target = EntityList.createEntityByName(task.idName, mc.theWorld);
					target.readFromNBT(task.targetTags);
				} catch(Exception e)
				{
					BQ_Standard.logger.log(Level.ERROR, "An error occured while loading entity in UI", e);
				}
			}
		}
		
		int progress = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? task.getUsersProgress(QuestingAPI.getQuestingUUID(mc.thePlayer)) : task.getGlobalProgress();
		String tnm = !task.ignoreNBT && target != null? target.getName() : task.idName;
		String txt = I18n.format("bq_standard.gui.kill", tnm) + " " + progress + "/" + task.required;
		mc.fontRendererObj.drawString(txt, posX + sizeX/2 - mc.fontRendererObj.getStringWidth(txt)/2, posY, getTextColor());
	}

	@Override
	public void drawForeground(int mx, int my, float partialTick)
	{
	}

	@Override
	public void onMouseClick(int mx, int my, int click)
	{
	}

	@Override
	public void onMouseScroll(int mx, int my, int scroll)
	{
	}

	@Override
	public void onKeyTyped(char c, int keyCode)
	{
	}
	
}
