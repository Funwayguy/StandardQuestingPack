package bq_standard.client.gui.tasks;

import java.awt.Color;
import java.util.HashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.utils.RenderUtils;
import bq_standard.tasks.TaskLocation;

public class GuiTaskLocation extends GuiElement implements IGuiEmbedded
{
	static HashMap<Integer,String> dimNameCache;
	TaskLocation task;
	private Minecraft mc;
	
	private int posX = 0;
	private int posY = 0;
	private int sizeX = 0;
	private int sizeY = 0;
	
	public GuiTaskLocation(TaskLocation task, int posX, int posY, int sizeX, int sizeY)
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
		// Calculate compass direction
		double la = Math.atan2(task.y - mc.thePlayer.posY, task.x - mc.thePlayer.posX);
		int radius = Math.min(sizeY/2, sizeX/4) - 12;
		int dx = (int)(Math.cos(la) * radius);
		int dy = (int)(Math.sin(la) * -radius);
		int cx = posX + sizeX - radius - 12;
		int cy = posY + sizeY/2;
		
		// Text offset
		int i = sizeY/2 - radius;
		
		mc.fontRendererObj.drawString(task.name, posX, posY + i, getTextColor(), false);
		i += 12;
		
		if(!task.hideInfo)
		{
			if(task.range >= 0)
			{
				mc.fontRendererObj.drawString(I18n.format("bq_standard.gui.location", "(" + task.x + ", " + task.y + ", " + task.z + ")"), posX, posY + i, getTextColor(), false);
				i += 12;
				mc.fontRendererObj.drawString(I18n.format("bq_standard.gui.distance", (int)mc.thePlayer.getDistance(task.x, task.y, task.z) + "m"), posX, posY + i, getTextColor(), false);
				i += 12;
			}
			
			mc.fontRendererObj.drawString(I18n.format("bq_standard.gui.dimension", getDimName(task.dim)), posX, posY + i, getTextColor(), false);
			i += 12;
		}
		
		if(task.isComplete(QuestingAPI.getQuestingUUID(mc.thePlayer)))
		{
			mc.fontRendererObj.drawString(I18n.format("bq_standard.gui.found"), posX, posY + i, Color.GREEN.getRGB(), false);
		} else
		{
			mc.fontRendererObj.drawString(TextFormatting.BOLD + I18n.format("bq_standard.gui.undiscovered"), posX, posY + i, Color.RED.getRGB(), true);
		}
		
		Gui.drawRect(cx - radius, cy - radius, cx + radius, cy + radius, Color.BLACK.getRGB());
		RenderUtils.DrawLine(cx - radius, cy - radius, cx + radius, cy - radius, 4, Color.WHITE.getRGB());
		RenderUtils.DrawLine(cx - radius, cy - radius, cx - radius, cy + radius, 4, Color.WHITE.getRGB());
		RenderUtils.DrawLine(cx + radius, cy + radius, cx + radius, cy - radius, 4, Color.WHITE.getRGB());
		RenderUtils.DrawLine(cx + radius, cy + radius, cx - radius, cy + radius, 4, Color.WHITE.getRGB());
		mc.fontRendererObj.drawString(TextFormatting.BOLD + "N", cx - 4, cy - radius - 9, getTextColor());
		mc.fontRendererObj.drawString(TextFormatting.BOLD + "S", cx - 4, cy + radius + 2, getTextColor());
		mc.fontRendererObj.drawString(TextFormatting.BOLD + "E", cx + radius + 2, cy - 4, getTextColor());
		mc.fontRendererObj.drawString(TextFormatting.BOLD + "W", cx - radius - 8, cy - 4, getTextColor());
		
		if(task.hideInfo || task.range < 0 || mc.thePlayer.dimension != task.dim)
		{
			GlStateManager.pushMatrix();
			GlStateManager.scale(2F, 2F, 2F);
			mc.fontRendererObj.drawString(TextFormatting.BOLD + "?", cx/2 - 4, cy/2 - 4 , Color.RED.getRGB());
			GlStateManager.popMatrix();
		} else
		{
			RenderUtils.DrawLine(cx, cy, cx + dx, cy + dy, 4, Color.RED.getRGB());
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
