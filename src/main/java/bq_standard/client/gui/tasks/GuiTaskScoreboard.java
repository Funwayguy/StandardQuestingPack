package bq_standard.client.gui.tasks;

import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
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
		GL11.glPushMatrix();
		GL11.glTranslatef(posX + sizeX/2, posY + sizeY/2, 0F);
		GL11.glScalef(2F, 2F, 1F);
		
		int tw1 = screen.mc.fontRenderer.getStringWidth(EnumChatFormatting.BOLD + task.scoreName);
		screen.mc.fontRenderer.drawString(EnumChatFormatting.BOLD + task.scoreName, -tw1/2, -12, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		Scoreboard board = screen.mc.thePlayer.getWorldScoreboard();
		ScoreObjective scoreObj = board == null? null : board.getObjective(task.scoreName);
		Score score = scoreObj == null? null : board.func_96529_a(screen.mc.thePlayer.getCommandSenderName(), scoreObj);
		String value = score == null? "?" : "" + score.getScorePoints();
		
		String txt = EnumChatFormatting.BOLD + value + " " + EnumChatFormatting.RESET + task.operation.GetText() + " " + task.target;
		
		if(score != null && task.operation.checkValues(score.getScorePoints(), task.target))
		{
			txt = EnumChatFormatting.GREEN + txt;
		} else
		{
			txt = EnumChatFormatting.RED + txt;
		}
		
		int tw2 = screen.mc.fontRenderer.getStringWidth(txt);
		screen.mc.fontRenderer.drawString(txt, -tw2/2, 1, ThemeRegistry.curTheme().textColor().getRGB(), false);
		GL11.glPopMatrix();
	}
}
