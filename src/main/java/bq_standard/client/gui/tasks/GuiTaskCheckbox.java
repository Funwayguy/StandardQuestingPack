package bq_standard.client.gui.tasks;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import betterquesting.api.ExpansionAPI;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.quests.IQuest;
import betterquesting.database.QuestDatabase;
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
		
		if(task != null && task.isComplete(mc.thePlayer.getGameProfile().getId()))
		{
			btn = new GuiButtonThemed(0, posX + sizeX/2 - 20, posY + sizeY/2 - 20, 40, 40, EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "\u2713");
			btn.enabled = false;
		} else if(task != null)
		{
			for(IQuest q : QuestDatabase.INSTANCE.getAllValues())
			{
				int tmp = q.getTasks().getKey(task);
				
				if(tmp >= 0)
				{
					tId = tmp;
					qId = QuestDatabase.INSTANCE.getKey(q);
					break;
				}
			}
			
			if(qId < 0)
			{
				btn = new GuiButtonThemed(0, posX + sizeX/2 - 20, posY + sizeY/2 - 20, 40, 40, "?");
				btn.enabled = false;
			} else
			{
				btn = new GuiButtonThemed(0, posX + sizeX/2 - 20, posY + sizeY/2 - 20, 40, 40, EnumChatFormatting.RED + "" + EnumChatFormatting.BOLD + "x");
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
			btn.displayString = EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "\u2713";
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", 2);
			tags.setInteger("qId", qId);
			tags.setInteger("tId", tId);
			ExpansionAPI.getAPI().getPacketSender().sendToServer(new PreparedPayload(StandardPacketType.CHECKBOX.GetLocation(), tags));
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
