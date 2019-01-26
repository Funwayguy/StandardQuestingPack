package bq_standard.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import bq_standard.network.StandardPacketType;
import bq_standard.tasks.TaskCheckbox;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class PktHandlerCheckbox implements IPacketHandler
{
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
	{
		int qId = !data.hasKey("qId", 99)? -1 : data.getInteger("qId");
		int tId = qId < 0 || !data.hasKey("tId", 99)? -1 : data.getInteger("tId");
		
		if(qId >= 0 && tId >= 0)
		{
            QuestCache qc = sender.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
            IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(qId);
            ITask task = quest == null ? null : quest.getTasks().getValue(tId);
            
            if(task instanceof TaskCheckbox)
            {
                task.setComplete(QuestingAPI.getQuestingUUID(sender));
                if(qc != null) qc.markQuestDirty(qId);
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
