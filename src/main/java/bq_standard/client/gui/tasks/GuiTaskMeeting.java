package bq_standard.client.gui.tasks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.utils.RenderUtils;
import bq_standard.tasks.TaskMeeting;

public class GuiTaskMeeting extends GuiElement implements IGuiEmbedded
{
	TaskMeeting task;
	Entity target;
	private Minecraft mc;
	
	private int posX = 0;
	private int posY = 0;
	private int sizeX = 0;
	private int sizeY = 0;
	
	public GuiTaskMeeting(TaskMeeting task, int posX, int posY, int sizeX, int sizeY)
	{
		this.mc = Minecraft.getMinecraft();
		this.task = task;
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
			if(EntityList.stringToClassMapping.containsKey(task.idName))
			{
				target = EntityList.createEntityByName(task.idName, mc.theWorld);
				target.readFromNBT(task.targetTags);
			}
		}
		
		String tnm = !task.ignoreNBT && target != null? target.getCommandSenderName() : task.idName;
		String txt = I18n.format("bq_standard.gui.meet", tnm) + " x" + task.amount;
		mc.fontRenderer.drawString(txt, posX + sizeX/2 - mc.fontRenderer.getStringWidth(txt)/2, posY, getTextColor());
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
