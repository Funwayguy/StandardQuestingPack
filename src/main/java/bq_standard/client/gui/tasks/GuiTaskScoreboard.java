package bq_standard.client.gui.tasks;

import java.text.DecimalFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import bq_standard.ScoreboardBQ;
import bq_standard.tasks.TaskScoreboard;

public class GuiTaskScoreboard extends GuiElement implements IGuiEmbedded
{
	private final TaskScoreboard task;
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
		GlStateManager.pushMatrix();
		GlStateManager.translate(posX + sizeX/2, posY + sizeY/2, 0F);
		GlStateManager.scale(2F, 2F, 1F);
		
		int tw1 = mc.fontRendererObj.getStringWidth(TextFormatting.BOLD + task.scoreName);
		mc.fontRendererObj.drawString(TextFormatting.BOLD + task.scoreDisp, -tw1/2, -12, getTextColor(), false);
		int score = ScoreboardBQ.getScore(QuestingAPI.getQuestingUUID(mc.thePlayer), task.scoreName);
		DecimalFormat df = new DecimalFormat("0.##");
		String value = df.format(score/task.conversion) + task.suffix;
		
		String txt = TextFormatting.BOLD + value + " " + TextFormatting.RESET + task.operation.GetText() + " " + df.format(task.target/task.conversion) + task.suffix;
		
		if(task.operation.checkValues(score, task.target))
		{
			txt = TextFormatting.GREEN + txt;
		} else
		{
			txt = TextFormatting.RED + txt;
		}
		
		int tw2 = mc.fontRendererObj.getStringWidth(txt);
		mc.fontRendererObj.drawString(txt, -tw2/2, 1, getTextColor(), false);
		GlStateManager.popMatrix();
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
