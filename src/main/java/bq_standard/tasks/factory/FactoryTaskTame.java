package bq_standard.tasks.factory;

import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskTame;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public final class FactoryTaskTame implements IFactory<TaskTame>
{
    public static final FactoryTaskTame INSTANCE = new FactoryTaskTame();
    
	private final ResourceLocation REG_ID = new ResourceLocation(BQ_Standard.MODID, "tame");
	
    @Override
    public ResourceLocation getRegistryName()
    {
        return REG_ID;
    }
    
    @Override
    public TaskTame createNew()
    {
        return new TaskTame();
    }
    
    @Override
    public TaskTame loadFromNBT(NBTTagCompound nbt)
    {
        TaskTame task = new TaskTame();
        task.readFromNBT(nbt);
        return task;
    }
}
