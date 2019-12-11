package bq_standard.importers.ftbq.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import bq_standard.importers.ftbq.FTBQQuestImporter;
import bq_standard.tasks.TaskLocation;
import net.minecraft.init.Blocks;
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
    
        FTBQQuestImporter.provideQuestIcon(new BigItemStack(Blocks.PORTAL));
        
        return new ITask[]{task};
    }
}
