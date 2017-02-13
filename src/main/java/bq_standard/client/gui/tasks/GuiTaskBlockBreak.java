package bq_standard.client.gui.tasks;

import java.util.UUID;
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
import bq_standard.tasks.TaskBlockBreak;

public class GuiTaskBlockBreak extends GuiElement implements IGuiEmbedded
{
	private GuiScrollingItems scrollList;
	private Minecraft mc;
	
	public GuiTaskBlockBreak(TaskBlockBreak task, IQuest quest, int posX, int posY, int sizeX, int sizeY)
	{
		this.mc = Minecraft.getMinecraft();
		
		scrollList = new GuiScrollingItems(mc, posX, posY + 16, sizeX, sizeY - 16);
		
		if(task == null)
		{
			return;
		}
		
		UUID playerID = QuestingAPI.getQuestingUUID(mc.player);
		
		int[] progress = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? task.getPartyProgress(playerID) : task.getGlobalProgress();
		
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
			
			if(progress[i] >= stack.stackSize || task.isComplete(playerID))
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
