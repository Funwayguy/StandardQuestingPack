package bq_standard.client.gui.tasks;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.BigItemStack;
import bq_standard.client.gui.GuiScrollingItems;
import bq_standard.tasks.TaskCrafting;

public class GuiTaskCrafting extends GuiEmbedded
{
	GuiScrollingItems scrollList;
	QuestInstance quest;
	TaskCrafting task;
	
	public GuiTaskCrafting(QuestInstance quest, TaskCrafting task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
		this.quest = quest;
		
		scrollList = new GuiScrollingItems(screen, sizeX, sizeY, posY, posX);
		
		if(task == null)
		{
			return;
		}
		
		int[] progress = quest == null || !quest.globalQuest? task.GetUserProgress(screen.mc.thePlayer.getUniqueID()) : task.GetGlobalProgress();
		
		for(int i = 0; i < task.requiredItems.size(); i++)
		{
			BigItemStack stack = task.requiredItems.get(i);
			
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
				txt += "\n" + TextFormatting.GREEN + I18n.format("betterquesting.tooltip.complete");
			} else
			{
				txt += "\n" + TextFormatting.RED + I18n.format("betterquesting.tooltip.incomplete");
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
