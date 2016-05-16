package bq_standard.client.gui.tasks;

import net.minecraft.client.renderer.GlStateManager;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.RenderUtils;
import bq_standard.tasks.TaskBlockBreak;

public class GuiTaskBlockBreak extends GuiEmbedded
{
	QuestInstance quest;
	TaskBlockBreak task;
	
	public GuiTaskBlockBreak(QuestInstance quest, TaskBlockBreak task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
		this.quest = quest;
	}

	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		BigItemStack dispStack = new BigItemStack(task.targetBlock, 1, task.targetMeta);
		int progress = quest == null || !quest.globalQuest? task.GetUserProgress(screen.mc.thePlayer.getUniqueID()) : task.GetGlobalProgress();
		
		if(dispStack.getBaseStack() != null)
		{
			screen.mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
			GlStateManager.disableDepth();
			screen.drawTexturedModalRect(posX + sizeX/2 - 9, posY + sizeY/2 - 18, 0, 48, 18, 18);
			GlStateManager.enableDepth();
			RenderUtils.RenderItemStack(screen.mc, dispStack.getBaseStack(), posX + sizeX/2 - 8, posY + sizeY/2 - 17, "");
			String txt = progress + "/" + task.targetNum;
			screen.mc.fontRendererObj.drawString(txt, posX + sizeX/2 - screen.mc.fontRendererObj.getStringWidth(txt)/2, posY + sizeY/2 + 2, ThemeRegistry.curTheme().textColor().getRGB());
			
			if(mx >= posX + sizeX/2 - 8 && mx < posX + sizeX/2 + 8 && my >= posY + sizeY/2 - 17 && my < posY + sizeY/2 - 1)
			{
				screen.DrawTooltip(dispStack.getBaseStack().getTooltip(screen.mc.thePlayer, screen.mc.gameSettings.advancedItemTooltips), mx, my);
			}
		}
	}
}
