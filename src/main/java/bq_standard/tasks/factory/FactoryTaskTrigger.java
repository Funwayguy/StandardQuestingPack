package bq_standard.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskTrigger;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskTrigger implements IFactoryData<ITask, NBTTagCompound>
{
    public static final FactoryTaskTrigger INSTANCE = new FactoryTaskTrigger();
    
	private final ResourceLocation REG_ID = new ResourceLocation(BQ_Standard.MODID, "trigger");
	
    @Override
    public ResourceLocation getRegistryName()
    {
        return REG_ID;
    }
    
    @Override
    public TaskTrigger createNew()
    {
        return new TaskTrigger();
    }
    
    @Override
    public TaskTrigger loadFromData(NBTTagCompound nbt)
    {
        TaskTrigger task = new TaskTrigger();
        task.readFromNBT(nbt);
        return task;
    }
}
