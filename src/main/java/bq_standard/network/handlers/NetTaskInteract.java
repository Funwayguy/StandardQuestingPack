package bq_standard.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.storage.DBEntry;
import bq_standard.tasks.TaskInteractItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NetTaskInteract
{
	private static final ResourceLocation ID_NAME = new ResourceLocation("bq_standard:task_interact");
	
	public static void registerHandler()
    {
        QuestingAPI.getAPI(ApiReference.PACKET_REG).registerServerHandler(ID_NAME, NetTaskInteract::onServer);
    }
    
    @SideOnly(Side.CLIENT)
    public static void requestInteraction(boolean isHit, boolean isMainHand)
    {
        NBTTagCompound payload = new NBTTagCompound();
        payload.setBoolean("isMainHand", isMainHand);
        payload.setBoolean("isHit", isHit);
        QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(ID_NAME, payload));
    }
    
	private static void onServer(Tuple<NBTTagCompound, EntityPlayerMP> message)
	{
	    EntityPlayerMP sender = message.getSecond();
	    NBTTagCompound tag = message.getFirst();
	    
        QuestCache qc = sender.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
		if(qc == null) return;
        
        EnumHand hand = tag.getBoolean("isMainHand") ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        boolean isHit = tag.getBoolean("isHit");
		
		for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(qc.getActiveQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskInteractItem) ((TaskInteractItem)task.getValue()).onInteract(entry, sender, hand, ItemStack.EMPTY, Blocks.AIR.getDefaultState(), sender.getPosition(), isHit);
            }
		}
    }
}
