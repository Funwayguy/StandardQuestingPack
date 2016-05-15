package bq_standard.client.gui.tasks;

import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.RenderUtils;
import bq_standard.tasks.TaskBlockBreak;

public class GuiTaskBlock extends GuiEmbedded
{
	QuestInstance quest;
	TaskBlockBreak task;
	
	public GuiTaskBlock(TaskBlockBreak task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
		
		for(QuestInstance q : QuestDatabase.questDB.values())
		{
			if(q != null && q.tasks.contains(task))
			{
				quest = q;
				break;
			}
		}
	}

	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		BigItemStack dispStack = new BigItemStack(task.targetBlock, 1, task.targetMeta);
		int progress = quest == null || !quest.globalQuest? task.GetUserProgress(screen.mc.thePlayer.getUniqueID()) : task.GetGlobalProgress();
		
		if(dispStack.getBaseStack() != null)
		{
			screen.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			screen.drawTexturedModalRect(posX + sizeX/2 - 9, posY + sizeY/2 - 18, 0, 48, 18, 18);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderUtils.RenderItemStack(screen.mc, dispStack.getBaseStack(), posX + sizeX/2 - 8, posY + sizeY/2 - 17, "");
			String txt = progress + "/" + task.targetNum;
			screen.mc.fontRenderer.drawString(txt, posX + sizeX/2 - screen.mc.fontRenderer.getStringWidth(txt)/2, posY + sizeY/2 + 2, ThemeRegistry.curTheme().textColor().getRGB());
			
			if(mx >= posX + sizeX/2 - 8 && mx < posX + sizeX/2 + 8 && my >= posY + sizeY/2 - 17 && my < posY + sizeY/2 - 1)
			{
				screen.DrawTooltip(dispStack.getBaseStack().getTooltip(screen.mc.thePlayer, screen.mc.gameSettings.advancedItemTooltips), mx, my);
			}
		}
	}
}
