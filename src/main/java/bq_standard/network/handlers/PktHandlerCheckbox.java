package bq_standard.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.network.handlers.PktHandler;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.tasks.TaskBase;
import bq_standard.tasks.TaskCheckbox;

public class PktHandlerCheckbox extends PktHandler
{
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data)
	{
		int qId = !data.hasKey("qId")? -1 : data.getInteger("qId");
		int tId = qId < 0 && !data.hasKey("tId")? -1 : data.getInteger("tId");
		
		if(qId >= 0 && tId >= 0)
		{
			try
			{
				TaskBase task = QuestDatabase.getQuestByID(qId).tasks.get(tId);
				
				if(task instanceof TaskCheckbox)
				{
					task.setCompletion(sender.getUniqueID(), true);
				}
			} catch(Exception e)
			{
				return;
			}
		}
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
	}
	
}
