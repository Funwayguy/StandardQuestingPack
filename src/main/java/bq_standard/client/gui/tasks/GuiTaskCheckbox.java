package bq_standard.client.gui.tasks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.network.PacketAssembly;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import bq_standard.network.StandardPacketType;
import bq_standard.tasks.TaskCheckbox;

public class GuiTaskCheckbox extends GuiEmbedded
{
	GuiButtonQuesting btn;
	TaskCheckbox task;
	int qId = -1;
	int tId = -1;
	
	public GuiTaskCheckbox(TaskCheckbox task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
		
		if(task != null && task.isComplete(screen.mc.thePlayer.getUniqueID()))
		{
			btn = new GuiButtonQuesting(0, posX + sizeX/2 - 20, posY + sizeY/2 - 20, 40, 40, TextFormatting.GREEN + "" + TextFormatting.BOLD + "\u2713");
			btn.enabled = false;
		} else if(task != null)
		{
			for(QuestInstance q : QuestDatabase.questDB.values())
			{
				int tmp = q.tasks.indexOf(task);
				
				if(tmp >= 0)
				{
					tId = tmp;
					qId = q.questID;
					break;
				}
			}
			
			if(qId < 0)
			{
				btn = new GuiButtonQuesting(0, posX + sizeX/2 - 20, posY + sizeY/2 - 20, 40, 40, "?");
				btn.enabled = false;
			} else
			{
				btn = new GuiButtonQuesting(0, posX + sizeX/2 - 20, posY + sizeY/2 - 20, 40, 40, TextFormatting.RED + "" + TextFormatting.BOLD + "x");
			}
		} else
		{
			btn = new GuiButtonQuesting(0, posX + sizeX/2 - 20, posY + sizeY/2 - 20, 40, 40, "?");
			btn.enabled = false;
		}
	}
	
	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		btn.drawButton(screen.mc, mx, my);
	}
	
	@Override
	public void mouseClick(int mx, int my, int click)
	{
		if(btn.enabled && btn.visible && btn.mousePressed(screen.mc, mx, my))
		{
			btn.enabled = false;
			btn.displayString = TextFormatting.GREEN + "" + TextFormatting.BOLD + "\u2713";
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", 2);
			tags.setInteger("qId", qId);
			tags.setInteger("tId", tId);
			PacketAssembly.SendToServer(StandardPacketType.CHECKBOX.GetLocation(), tags);
		}
	}
}
