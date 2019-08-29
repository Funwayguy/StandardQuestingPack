package bq_standard.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import bq_standard.handlers.EventHandler;
import bq_standard.tasks.TaskInteractItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

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
		
		List<EntityPlayer> actParty = EventHandler.getActiveParty(sender);
		List<DBEntry<IQuest>> actQuest = QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(EventHandler.getSharedQuests(actParty));
		List<Integer> dirty = new ArrayList<>();
        
        EnumHand hand = tag.getBoolean("isMainHand") ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        boolean isHit = tag.getBoolean("isHit");
		
		for(DBEntry<IQuest> entry : actQuest)
		{
		    Runnable callback = () -> dirty.add(entry.getID());
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof TaskInteractItem) ((TaskInteractItem)task.getValue()).onInteract(sender, hand, ItemStack.EMPTY, Blocks.AIR.getDefaultState(), sender.getPosition(), isHit, callback);
            }
		}
		
		if(dirty.size() > 0) EventHandler.bulkMarkDirtyPlayer(actParty, dirty);
    }
}
