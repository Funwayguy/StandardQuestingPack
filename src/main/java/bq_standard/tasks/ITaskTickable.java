package bq_standard.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;

public interface ITaskTickable extends ITask
{
    void tickTask(@Nonnull DBEntry<IQuest> quest, @Nonnull EntityPlayer player);
}
