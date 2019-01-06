package bq_standard.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import net.minecraft.entity.player.EntityPlayer;

public interface ITaskTickable extends ITask
{
    void tickTask(IQuest quest, EntityPlayer player);
}
