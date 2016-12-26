package bq_standard.client.gui.tasks;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import bq_standard.network.StandardPacketType;
import bq_standard.tasks.TaskCheckbox;

public class GuiTaskCheckbox extends GuiElement implements IGuiEmbedded
{
	private Minecraft mc;
	private GuiButtonThemed btn;
	private int qId = -1;
	private int tId = -1;
	
	public GuiTaskCheckbox(TaskCheckbox task, int posX, int posY, int sizeX, int sizeY)
	{
		this.mc = Minecraft.getMinecraft();
		
		if(task != null && task.isComplete(QuestingAPI.getQuestingUUID(mc.thePlayer)))
		{
			btn = new GuiButtonThemed(0, posX + sizeX/2 - 20, posY + sizeY/2 - 20, 40, 40, TextFormatting.GREEN + "" + TextFormatting.BOLD + "\u2713");
			btn.enabled = false;
		} else if(task != null)
		{
			for(IQuest q : QuestingAPI.getAPI(ApiReference.QUEST_DB).getAllValues())
			{
				int tmp = q.getTasks().getKey(task);
				
				if(tmp >= 0)
				{
					tId = tmp;
					qId = QuestingAPI.getAPI(ApiReference.QUEST_DB).getKey(q);
					break;
				}
			}
			
			if(qId < 0)
			{
				btn = new GuiButtonThemed(0, posX + sizeX/2 - 20, posY + sizeY/2 - 20, 40, 40, "?");
				btn.enabled = false;
			} else
			{
				btn = new GuiButtonThemed(0, posX + sizeX/2 - 20, posY + sizeY/2 - 20, 40, 40, TextFormatting.RED + "" + TextFormatting.BOLD + "x");
			}
		} else
		{
			btn = new GuiButtonThemed(0, posX + sizeX/2 - 20, posY + sizeY/2 - 20, 40, 40, "?");
			btn.enabled = false;
		}
	}
	
	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		btn.drawButton(mc, mx, my);
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(btn.enabled && btn.visible && btn.mousePressed(mc, mx, my))
		{
			btn.enabled = false;
			btn.displayString = TextFormatting.GREEN + "" + TextFormatting.BOLD + "\u2713";
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", 2);
			tags.setInteger("qId", qId);
			tags.setInteger("tId", tId);
			QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(StandardPacketType.CHECKBOX.GetLocation(), tags));
		}
	}

	@Override
	public void drawForeground(int mx, int my, float partialTick)
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
