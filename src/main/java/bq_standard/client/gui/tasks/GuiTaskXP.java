package bq_standard.client.gui.tasks;

import java.awt.Color;
import org.lwjgl.opengl.GL11;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.utils.RenderUtils;
import bq_standard.XPHelper;
import bq_standard.tasks.TaskXP;

public class GuiTaskXP extends GuiEmbedded
{
	ItemStack bottle = new ItemStack(Items.experience_bottle);
	TaskXP task;
	
	public GuiTaskXP(TaskXP task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
	}
	
	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		int barSize = Math.min(sizeX/2, 128);
		int xp = 0;
		int barProg = 0;
		int midX = sizeX/2;
		
		if(!task.consume)
		{
			if(task.levels)
			{
				xp = screen.mc.thePlayer.experienceLevel;
			} else
			{
				xp = XPHelper.getPlayerXP(screen.mc.thePlayer);
			}
		} else
		{
			Integer progress = task.userProgress.get(screen.mc.thePlayer.getUniqueID());
			xp = progress == null? 0 : progress;
		}
		
		barProg = (int)(MathHelper.clamp_float(xp/(float)task.amount, 0F, 1F) * (barSize - 2));
		
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glScalef(2F, 2F, 2F);
		RenderUtils.RenderItemStack(screen.mc, bottle, (posX + sizeX/2 - 16)/2, (posY + sizeY/2 - 32)/2, "");
		GL11.glPopMatrix();
		GuiQuesting.drawRect(posX + midX - barSize/2, posY + sizeY/2, posX + midX + barSize/2, posY + sizeY/2 + 16, Color.BLACK.getRGB());
		GuiQuesting.drawRect(posX + midX - barSize/2 + 1, posY + sizeY/2 + 1, posX + midX - barSize/2 + barProg + 1, posY + sizeY/2 + 15, Color.GREEN.getRGB());
		String txt = TextFormatting.BOLD + "" + xp + "/" + task.amount + (task.levels? "L" : "XP");
		screen.mc.fontRendererObj.drawString(txt, posX + sizeX/2 - screen.mc.fontRendererObj.getStringWidth(txt)/2, posY + sizeY/2 + 4, Color.WHITE.getRGB(), true);
	}
	
	/*
	 * Bottle Icon
	 * Progress Text & Bar
	 */
}
