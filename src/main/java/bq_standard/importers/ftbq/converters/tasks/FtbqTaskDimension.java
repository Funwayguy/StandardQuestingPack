package bq_standard.importers.ftbq.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import bq_standard.tasks.TaskLocation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;

public class FtbqTaskDimension
{
    public ITask[] converTask(NBTTagCompound tag)
    {
        TaskLocation task = new TaskLocation();
        
        task.range = -1;
        task.dim = tag.getInteger("dim");
        task.x = 0;
        task.y = 0;
        task.z = 0;
        task.visible = true;
        task.name = tag.hasKey("title", 8) ? tag.getString("title") : DimensionType.getById(task.dim).getName();
        
        return new ITask[]{task};
    }
}
