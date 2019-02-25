package bq_standard.importers.ftbq.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import bq_standard.tasks.TaskScoreboard;
import bq_standard.tasks.TaskScoreboard.ScoreOperation;
import net.minecraft.nbt.NBTTagCompound;

public class FtbqTaskStat
{
    public ITask[] convertTask(NBTTagCompound tag)
    {
        TaskScoreboard task = new TaskScoreboard();
        
        task.scoreName = tag.getString("stat");
        task.scoreDisp = tag.hasKey("title", 8) ? tag.getString("title") : task.scoreName;
        task.operation = ScoreOperation.MORE_OR_EQUAL;
        task.target = tag.getInteger("value");
        
        return new ITask[]{task};
    }
}
