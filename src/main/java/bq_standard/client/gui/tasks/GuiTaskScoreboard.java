package bq_standard.client.gui.tasks;

import java.text.DecimalFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.IGuiEmbedded;
import bq_standard.ScoreboardBQ;
import bq_standard.tasks.TaskScoreboard;

public class GuiTaskScoreboard extends GuiElement implements IGuiEmbedded
{
	TaskScoreboard task;
	private Minecraft mc;
	
	private int posX = 0;
	private int posY = 0;
	private int sizeX = 0;
	private int sizeY = 0;
	
	public GuiTaskScoreboard(TaskScoreboard task, int posX, int posY, int sizeX, int sizeY)
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
		GL11.glPushMatrix();
		GL11.glTranslatef(posX + sizeX/2, posY + sizeY/2, 0F);
		GL11.glScalef(2F, 2F, 1F);
		
		int tw1 = mc.fontRenderer.getStringWidth(EnumChatFormatting.BOLD + task.scoreName);
		mc.fontRenderer.drawString(EnumChatFormatting.BOLD + task.scoreName, -tw1/2, -12, getTextColor(), false);
		int score = ScoreboardBQ.getScore(mc.thePlayer.getGameProfile().getId(), task.scoreName);
		DecimalFormat df = new DecimalFormat("0.##");
		String value = df.format(score/task.conversion) + task.suffix;
		
		String txt = EnumChatFormatting.BOLD + value + " " + EnumChatFormatting.RESET + task.operation.GetText() + " " + df.format(task.target/task.conversion) + task.suffix;
		
		if(task.operation.checkValues(score, task.target))
		{
			txt = EnumChatFormatting.GREEN + txt;
		} else
		{
			txt = EnumChatFormatting.RED + txt;
		}
		
		int tw2 = mc.fontRenderer.getStringWidth(txt);
		mc.fontRenderer.drawString(txt, -tw2/2, 1, getTextColor(), false);
		GL11.glPopMatrix();
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
