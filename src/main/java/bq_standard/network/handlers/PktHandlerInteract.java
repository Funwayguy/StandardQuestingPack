package bq_standard.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.storage.DBEntry;
import bq_standard.network.StandardPacketType;
import bq_standard.tasks.TaskInteractItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class PktHandlerInteract implements IPacketHandler
{
    @Override
    public ResourceLocation getRegistryName()
    {
        return StandardPacketType.INTERACT.GetLocation();
    }
    
    @Override
    public void handleServer(NBTTagCompound tag, EntityPlayerMP sender)
    {
        QuestCache qc = (QuestCache)sender.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
		if(qc == null) return;
    
        boolean isHit = tag.getBoolean("isHit");
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskInteractItem) ((TaskInteractItem)task.getValue()).onInteract(entry, sender, null, Blocks.air, 0, (int)sender.posX, (int)sender.posY, (int)sender.posZ, isHit);
            }
		}
    }
    
    @Override
    public void handleClient(NBTTagCompound tag){}
}
