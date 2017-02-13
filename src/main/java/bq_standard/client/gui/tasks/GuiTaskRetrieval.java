package bq_standard.client.gui.tasks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.lists.GuiScrollingItems;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.BigItemStack;
import bq_standard.tasks.TaskRetrieval;

public class GuiTaskRetrieval extends GuiElement implements IGuiEmbedded
{
	GuiScrollingItems scrollList;
	IQuest quest;
	TaskRetrieval task;
	private Minecraft mc;
	
	private int posX = 0;
	private int posY = 0;
	
	public GuiTaskRetrieval(TaskRetrieval task, IQuest quest, int posX, int posY, int sizeX, int sizeY)
	{
		this.mc = Minecraft.getMinecraft();
		this.task = task;
		this.posX = posX;
		this.posY = posY;
		
		scrollList = new GuiScrollingItems(mc, posX, posY + 16, sizeX, sizeY - 16);
		
		if(task == null)
		{
			return;
		}
		
		int[] progress = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? task.getPartyProgress(QuestingAPI.getQuestingUUID(mc.player)) : task.getGlobalProgress();
		
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
			
			if(task.consume)
			{
				txt = txt + progress[i] + "/" + stack.stackSize;
			} else
			{
				txt = txt + stack.stackSize;
			}
			
			if(progress[i] >= stack.stackSize || task.isComplete(QuestingAPI.getQuestingUUID(mc.player)))
			{
				txt += "\n" + TextFormatting.GREEN + I18n.format("betterquesting.tooltip.complete");
			} else
			{
				txt += "\n" + TextFormatting.RED + I18n.format("betterquesting.tooltip.incomplete");
			}
			
			scrollList.addItem(stack, txt);
		}
	}

	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		String sCon = (task.consume? TextFormatting.RED : TextFormatting.GREEN) + I18n.format(task.consume? "gui.yes" : "gui.no");
		mc.fontRendererObj.drawString(I18n.format("bq_standard.btn.consume", sCon), posX, posY, getTextColor(), false);
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
