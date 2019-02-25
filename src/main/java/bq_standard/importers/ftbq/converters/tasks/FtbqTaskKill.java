package bq_standard.importers.ftbq.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import bq_standard.tasks.TaskHunt;
import net.minecraft.nbt.NBTTagCompound;

public class FtbqTaskKill
{
    public ITask[] convertTask(NBTTagCompound tag)
    {
        TaskHunt task = new TaskHunt();
        
        task.idName = tag.getString("entity");
        task.targetTags = new NBTTagCompound();
        task.required = tag.getInteger("value");
        task.ignoreNBT = true;
        task.subtypes = true;
        
        return new ITask[]{task};
    }
}
