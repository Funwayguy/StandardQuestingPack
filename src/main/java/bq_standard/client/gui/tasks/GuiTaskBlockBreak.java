package bq_standard.client.gui.tasks;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.BigItemStack;
import bq_standard.client.gui.GuiScrollingItems;
import bq_standard.tasks.TaskBlockBreak;

public class GuiTaskBlockBreak extends GuiEmbedded
{
	GuiScrollingItems scrollList;
	QuestInstance quest;
	TaskBlockBreak task;
	
	public GuiTaskBlockBreak(QuestInstance quest, TaskBlockBreak task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
		this.quest = quest;
		scrollList = new GuiScrollingItems(screen, sizeX, sizeY - 16, posY + 16, posX);
		
		if(task == null)
		{
			return;
		}
		
		int[] progress = quest == null || !quest.globalQuest? task.GetUserProgress(screen.mc.thePlayer.getUniqueID()) : task.GetGlobalProgress();
		
		for(int i = 0; i < task.blockTypes.size(); i++)
		{
			BigItemStack stack = task.blockTypes.get(i).getItemStack();
			
			if(stack == null)
			{
				continue;
			}
			
			String txt = stack.getBaseStack().getDisplayName();
			
			if(stack.oreDict.length() > 0)
			{
				txt += " (" + stack.oreDict + ")";
			}
			
			txt += "\n";
			
			txt = txt + progress[i] + "/" + stack.stackSize;
			
			if(progress[i] >= stack.stackSize || task.isComplete(screen.mc.thePlayer.getUniqueID()))
			{
				txt += "\n" + EnumChatFormatting.GREEN + I18n.format("betterquesting.tooltip.complete");
			} else
			{
				txt += "\n" + EnumChatFormatting.RED + I18n.format("betterquesting.tooltip.incomplete");
			}
			
			scrollList.addEntry(stack, txt);
		}
	}

	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		scrollList.drawScreen(mx, my, partialTick);
	}
}
