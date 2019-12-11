package bq_standard.importers.ftbq.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import bq_standard.tasks.TaskTrigger;
import net.minecraft.nbt.NBTTagCompound;

public class FtbqTaskAdvancement
{
    public ITask[] converTask(NBTTagCompound tag)
    {
        TaskTrigger task = new TaskTrigger();
        
        task.setTriggerID(tag.getString("advancement"));
        task.setCriteriaJson(tag.getString("criterion"));
        
        return new ITask[]{task};
    }
}
