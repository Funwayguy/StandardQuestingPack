package bq_standard.tasks;

import betterquesting.api.questing.tasks.ITask;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;

public interface ITaskTickable extends ITask
{
    void tickTask(@Nonnull EntityPlayer player, Runnable callback);
}
