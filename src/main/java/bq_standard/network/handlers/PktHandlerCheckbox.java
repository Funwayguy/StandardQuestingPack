package bq_standard.network.handlers;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.quests.tasks.ITask;
import betterquesting.database.QuestDatabase;
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
				ITask task = QuestDatabase.INSTANCE.getValue(qId).getTasks().getValue(tId);
				
				if(task instanceof TaskCheckbox)
				{
					task.setComplete(sender.getGameProfile().getId());
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
