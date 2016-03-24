package bq_standard.client.gui.tasks;

import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import bq_standard.client.gui.GuiScrollingFluids;
import bq_standard.tasks.TaskFluid;

public class GuiTaskFluid extends GuiEmbedded
{
	GuiScrollingFluids scrollList;
	TaskFluid task;
	int scroll = 0;
	
	public GuiTaskFluid(TaskFluid task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
		scrollList = new GuiScrollingFluids(screen, sizeX, sizeY, posY, posX);
		
		if(task == null)
		{
			return;
		}
		
		int[] progress = task.userProgress.get(screen.mc.thePlayer.getUniqueID());
		progress = progress == null? new int[task.requiredFluids.size()] : progress;
		
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
				txt += "\n" + TextFormatting.GREEN + I18n.translateToLocal("betterquesting.tooltip.complete");
			} else
			{
				txt += "\n" + TextFormatting.RED + I18n.translateToLocal("betterquesting.tooltip.incomplete");
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
