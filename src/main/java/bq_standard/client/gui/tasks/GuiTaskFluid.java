package bq_standard.client.gui.tasks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.FluidStack;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.lists.GuiScrollingFluids;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import bq_standard.tasks.TaskFluid;

public class GuiTaskFluid implements IGuiEmbedded
{
	private Minecraft mc;
	GuiScrollingFluids scrollList;
	IQuest quest;
	TaskFluid task;
	int scroll = 0;
	
	public GuiTaskFluid(TaskFluid task, IQuest quest, int posX, int posY, int sizeX, int sizeY)
	{
		this.mc = Minecraft.getMinecraft();
		this.task = task;
		this.quest = quest;
		
		scrollList = new GuiScrollingFluids(mc, posX, posY, sizeX, sizeY);
		
		if(task == null)
		{
			return;
		}
		
		int[] progress = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? task.getUsersProgress(QuestingAPI.getQuestingUUID(mc.thePlayer)) : task.getGlobalProgress();
		
		for(int i = 0; i < task.requiredFluids.size(); i++)
		{
			FluidStack stack = task.requiredFluids.get(i);
			
			if(stack == null)
			{
				continue;
			}
			
			String txt = stack.getLocalizedName() + "\n";
			txt = txt + progress[i] + "/" + stack.amount + "mB";
			
			if(progress[i] >= stack.amount || task.isComplete(QuestingAPI.getQuestingUUID(mc.thePlayer)))
			{
				txt += "\n" + EnumChatFormatting.GREEN + I18n.format("betterquesting.tooltip.complete");
			} else
			{
				txt += "\n" + EnumChatFormatting.RED + I18n.format("betterquesting.tooltip.incomplete");
			}
			
			scrollList.addFluid(stack, txt);
		}
	}

	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		scrollList.drawBackground(mx, my, partialTick);
	}

	@Override
	public void drawForeground(int mx, int my, float partialTick)
	{
		scrollList.drawForeground(mx, my, partialTick);
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
