package bq_standard.client.gui.tasks;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.utils.BigItemStack;
import bq_standard.client.gui.GuiScrollingItems;
import bq_standard.tasks.TaskCrafting;

public class GuiTaskCrafting extends GuiEmbedded
{
	GuiScrollingItems scrollList;
	TaskCrafting task;
	
	public GuiTaskCrafting(TaskCrafting task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
		scrollList = new GuiScrollingItems(screen, sizeX, sizeY, posY, posX);
		
		if(task == null)
		{
			return;
		}
		
		int[] progress = task.userProgress.get(screen.mc.thePlayer.getUniqueID());
		progress = progress == null? new int[task.requiredItems.size()] : progress;
		
		for(int i = 0; i < task.requiredItems.size(); i++)
		{
			BigItemStack stack = task.requiredItems.get(i);
			
			if(stack == null)
			{
				continue;
			}
			
			String txt = stack.getBaseStack().getDisplayName() + "\n";
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
