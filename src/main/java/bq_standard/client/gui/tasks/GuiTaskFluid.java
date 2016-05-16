package bq_standard.client.gui.tasks;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.FluidStack;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.QuestInstance;
import bq_standard.client.gui.GuiScrollingFluids;
import bq_standard.tasks.TaskFluid;

public class GuiTaskFluid extends GuiEmbedded
{
	GuiScrollingFluids scrollList;
	QuestInstance quest;
	TaskFluid task;
	int scroll = 0;
	
	public GuiTaskFluid(QuestInstance quest, TaskFluid task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
		this.quest = quest;
		
		scrollList = new GuiScrollingFluids(screen, sizeX, sizeY, posY, posX);
		
		if(task == null)
		{
			return;
		}
		
		int[] progress = quest == null || !quest.globalQuest? task.GetUserProgress(screen.mc.thePlayer.getUniqueID()) : task.GetGlobalProgress();
		
		for(int i = 0; i < task.requiredFluids.size(); i++)
		{
			FluidStack stack = task.requiredFluids.get(i);
			
			if(stack == null)
			{
				continue;
			}
			
			String txt = stack.getLocalizedName() + "\n";
			txt = txt + progress[i] + "/" + stack.amount + "mB";
			
			if(progress[i] >= stack.amount || task.isComplete(screen.mc.thePlayer.getUniqueID()))
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
