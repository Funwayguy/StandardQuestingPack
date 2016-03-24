package bq_standard.client.gui.tasks;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.utils.RenderUtils;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskHunt;

public class GuiTaskHunt extends GuiEmbedded
{
	TaskHunt task;
	Entity target;
	
	public GuiTaskHunt(TaskHunt task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
	}

	@Override
	public void drawGui(int mx, int my, float partialTick)
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
			if(EntityList.stringToClassMapping.containsKey(task.idName))
			{
				try
				{
					target = EntityList.createEntityByName(task.idName, screen.mc.theWorld);
					target.readFromNBT(task.targetTags);
				} catch(Exception e)
				{
					BQ_Standard.logger.log(Level.ERROR, "An error occured while loading entity in UI", e);
				}
			}
		}
		
		Integer progress = task.userProgress.get(screen.mc.thePlayer.getUniqueID());
		progress = progress == null? 0 : progress;
		String tnm = !task.ignoreNBT && target != null? target.getName() : task.idName;
		String txt = I18n.translateToLocalFormatted("bq_standard.gui.kill", tnm) + " " + progress + "/" + task.required;
		screen.mc.fontRendererObj.drawString(txt, posX + sizeX/2 - screen.mc.fontRendererObj.getStringWidth(txt)/2, posY, ThemeRegistry.curTheme().textColor().getRGB());
	}
	
}
