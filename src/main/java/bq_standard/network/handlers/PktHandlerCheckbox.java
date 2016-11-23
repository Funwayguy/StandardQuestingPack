package bq_standard.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.questing.tasks.ITask;
import bq_standard.network.StandardPacketType;
import bq_standard.tasks.TaskCheckbox;

public class PktHandlerCheckbox implements IPacketHandler
{
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
		int qId = !data.hasKey("qId")? -1 : data.getInteger("qId");
		int tId = qId < 0 && !data.hasKey("tId")? -1 : data.getInteger("tId");
		
		if(qId >= 0 && tId >= 0)
		{
			try
			{
				ITask task = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(qId).getTasks().getValue(tId);
				
				if(task instanceof TaskCheckbox)
				{
					task.setComplete(QuestingAPI.getQuestingUUID(sender));
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

	@Override
	public ResourceLocation getRegistryName()
	{
		return StandardPacketType.CHECKBOX.GetLocation();
	}
	
}
