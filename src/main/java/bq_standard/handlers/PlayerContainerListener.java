package bq_standard.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import bq_standard.tasks.ITaskInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class PlayerContainerListener implements ICrafting
{
    private static final HashMap<UUID, PlayerContainerListener> LISTEN_MAP = new HashMap<>();
    
    static void refreshListener(@Nonnull EntityPlayer player)
    {
        UUID uuid = QuestingAPI.getQuestingUUID(player);
        PlayerContainerListener listener = LISTEN_MAP.get(uuid);
        if(listener != null)
        {
            listener.player = player;
        } else
        {
            listener = new PlayerContainerListener(player);
            LISTEN_MAP.put(uuid, listener);
        }
        
        try
        {
            player.inventoryContainer.addCraftingToCrafters(listener);
        } catch(Exception ignored){}
    }
    
    private EntityPlayer player;
    
    private PlayerContainerListener(@Nonnull EntityPlayer player)
    {
        this.player = player;
    }
    
    @Override
    public void sendContainerAndContentsToPlayer(Container container, List nonNullList)
    {
        updateTasks();
    }
    
    @Override
    public void sendSlotContents(Container container, int i, ItemStack itemStack)
    {
        updateTasks();
    }
    
    @Override
    public void sendProgressBarUpdate(Container container, int i, int i1){}
    
    private void updateTasks()
    {
        ParticipantInfo pInfo = new ParticipantInfo(player);
		
        for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof ITaskInventory) ((ITaskInventory)task.getValue()).onInventoryChange(entry, pInfo);
            }
		}
    }
}
