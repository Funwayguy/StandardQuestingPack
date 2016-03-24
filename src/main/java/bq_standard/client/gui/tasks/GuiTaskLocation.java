package bq_standard.client.gui.tasks;

import java.awt.Color;
import java.util.HashMap;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.utils.RenderUtils;
import bq_standard.tasks.TaskLocation;

public class GuiTaskLocation extends GuiEmbedded
{
	static HashMap<Integer,String> dimNameCache;
	TaskLocation task;
	
	public GuiTaskLocation(TaskLocation task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
	}

	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		// Calculate compass direction
		double la = Math.atan2(task.y - screen.mc.thePlayer.posY, task.x - screen.mc.thePlayer.posX);
		int radius = Math.min(sizeY/2, sizeX/4) - 12;
		int dx = (int)(Math.cos(la) * radius);
		int dy = (int)(Math.sin(la) * -radius);
		int cx = posX + sizeX - radius - 12;
		int cy = posY + sizeY/2;
		
		// Text offset
		int i = sizeY/2 - radius;
		
		screen.mc.fontRendererObj.drawString(task.name, posX, posY + i, ThemeRegistry.curTheme().textColor().getRGB(), false);
		i += 12;
		
		if(!task.hideInfo)
		{
			if(task.range >= 0)
			{
				screen.mc.fontRendererObj.drawString(I18n.translateToLocalFormatted("bq_standard.gui.location", "(" + task.x + ", " + task.y + ", " + task.z + ")"), posX, posY + i, ThemeRegistry.curTheme().textColor().getRGB(), false);
				i += 12;
				screen.mc.fontRendererObj.drawString(I18n.translateToLocalFormatted("bq_standard.gui.distance", (int)screen.mc.thePlayer.getDistance(task.x, task.y, task.z) + "m"), posX, posY + i, ThemeRegistry.curTheme().textColor().getRGB(), false);
				i += 12;
			}
			
			screen.mc.fontRendererObj.drawString(I18n.translateToLocalFormatted("bq_standard.gui.dimension", getDimName(task.dim)), posX, posY + i, ThemeRegistry.curTheme().textColor().getRGB(), false);
			i += 12;
		}
		
		if(task.isComplete(screen.mc.thePlayer.getUniqueID()))
		{
			screen.mc.fontRendererObj.drawString(I18n.translateToLocal("bq_standard.gui.found"), posX, posY + i, Color.GREEN.getRGB(), false);
		} else
		{
			screen.mc.fontRendererObj.drawString(TextFormatting.BOLD + I18n.translateToLocal("bq_standard.gui.undiscovered"), posX, posY + i, Color.RED.getRGB(), true);
		}
		
		Gui.drawRect(cx - radius, cy - radius, cx + radius, cy + radius, Color.BLACK.getRGB());
		RenderUtils.DrawLine(cx - radius, cy - radius, cx + radius, cy - radius, 4, Color.WHITE);
		RenderUtils.DrawLine(cx - radius, cy - radius, cx - radius, cy + radius, 4, Color.WHITE);
		RenderUtils.DrawLine(cx + radius, cy + radius, cx + radius, cy - radius, 4, Color.WHITE);
		RenderUtils.DrawLine(cx + radius, cy + radius, cx - radius, cy + radius, 4, Color.WHITE);
		screen.mc.fontRendererObj.drawString(TextFormatting.BOLD + "N", cx - 4, cy - radius - 9, ThemeRegistry.curTheme().textColor().getRGB());
		screen.mc.fontRendererObj.drawString(TextFormatting.BOLD + "S", cx - 4, cy + radius + 2, ThemeRegistry.curTheme().textColor().getRGB());
		screen.mc.fontRendererObj.drawString(TextFormatting.BOLD + "E", cx + radius + 2, cy - 4, ThemeRegistry.curTheme().textColor().getRGB());
		screen.mc.fontRendererObj.drawString(TextFormatting.BOLD + "W", cx - radius - 8, cy - 4, ThemeRegistry.curTheme().textColor().getRGB());
		
		if(task.hideInfo || task.range < 0 || screen.mc.thePlayer.dimension != task.dim)
		{
			GL11.glPushMatrix();
			GL11.glScalef(2F, 2F, 2F);
			screen.mc.fontRendererObj.drawString(TextFormatting.BOLD + "?", cx/2 - 4, cy/2 - 4 , Color.RED.getRGB());
			GL11.glPopMatrix();
		} else
		{
			RenderUtils.DrawLine(cx, cy, cx + dx, cy + dy, 4, Color.RED);
		}
	}
	
	/**
	 * Returns the name of the given dimension or a question mark if it is missing or unknown
	 */
	public static String getDimName(int dim)
	{
		if(dimNameCache == null)
		{
			dimNameCache = new HashMap<Integer,String>();
			Integer[] dimNums = DimensionManager.getStaticDimensionIDs();
			
			for(Integer i : dimNums)
			{
				try
				{
					WorldProvider prov = DimensionManager.createProviderFor(i);
					
					if(prov == null)
					{
						continue;
					}
					
					dimNameCache.put(i, prov.getDimensionType().getName());
				} catch(Exception e)
				{
					dimNameCache.put(i, "ERROR");
				}
			}
		}
		
		String name = dimNameCache.get(dim);
		
		return name != null? name : "?";
	}
}
