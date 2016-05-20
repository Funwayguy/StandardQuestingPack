package bq_standard.client.gui.tasks;

import java.text.DecimalFormat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import bq_standard.ScoreboardBQ;
import bq_standard.tasks.TaskScoreboard;

public class GuiTaskScoreboard extends GuiEmbedded
{
	TaskScoreboard task;
	
	public GuiTaskScoreboard(TaskScoreboard task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
	}

	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		GlStateManager.pushMatrix();
		GlStateManager.translate(posX + sizeX/2, posY + sizeY/2, 0F);
		GlStateManager.scale(2F, 2F, 1F);
		
		int tw1 = screen.mc.fontRendererObj.getStringWidth(EnumChatFormatting.BOLD + task.scoreName);
		screen.mc.fontRendererObj.drawString(EnumChatFormatting.BOLD + task.scoreName, -tw1/2, -12, ThemeRegistry.curTheme().textColor().getRGB(), false);
		int score = ScoreboardBQ.getScore(screen.mc.thePlayer.getUniqueID(), task.scoreName);
		DecimalFormat df = new DecimalFormat("0.##");
		String value = df.format(score/task.conversion) + task.suffix;
		
		String txt = EnumChatFormatting.BOLD + value + " " + EnumChatFormatting.RESET + task.operation.GetText() + " " + task.target;
		
		if(task.operation.checkValues(score, task.target))
		{
			txt = EnumChatFormatting.GREEN + txt;
		} else
		{
			txt = EnumChatFormatting.RED + txt;
		}
		
		int tw2 = screen.mc.fontRendererObj.getStringWidth(txt);
		screen.mc.fontRendererObj.drawString(txt, -tw2/2, 1, ThemeRegistry.curTheme().textColor().getRGB(), false);
		GlStateManager.popMatrix();
	}
}
