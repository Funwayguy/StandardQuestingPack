package bq_standard.client.gui.tasks;

import java.text.DecimalFormat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.TextFormatting;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
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
		
		int tw1 = screen.mc.fontRendererObj.getStringWidth(TextFormatting.BOLD + task.scoreName);
		screen.mc.fontRendererObj.drawString(TextFormatting.BOLD + task.scoreName, -tw1/2, -12, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		Scoreboard board = screen.mc.thePlayer.getWorldScoreboard();
		ScoreObjective scoreObj = board == null? null : board.getObjective(task.scoreName);
		Score score = scoreObj == null? null : board.getOrCreateScore(screen.mc.thePlayer.getName(), scoreObj);
		DecimalFormat df = new DecimalFormat("0.##");
		String value = score == null? "?" : df.format(score.getScorePoints()/task.conversion) + task.suffix;
		
		String txt = TextFormatting.BOLD + value + " " + TextFormatting.RESET + task.operation.GetText() + " " + task.target;
		
		if(score != null && task.operation.checkValues(score.getScorePoints(), task.target))
		{
			txt = TextFormatting.GREEN + txt;
		} else
		{
			txt = TextFormatting.RED + txt;
		}
		
		int tw2 = screen.mc.fontRendererObj.getStringWidth(txt);
		screen.mc.fontRendererObj.drawString(txt, -tw2/2, 1, ThemeRegistry.curTheme().textColor().getRGB(), false);
		GlStateManager.popMatrix();
	}
}
