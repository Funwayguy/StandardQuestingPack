package bq_standard.tasks.factory;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskAdvancement;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public final class FactoryTaskAdvancement implements IFactoryData<ITask, NBTTagCompound>
{
    public static final FactoryTaskAdvancement INSTANCE = new FactoryTaskAdvancement();
    
	private final ResourceLocation REG_ID = new ResourceLocation(BQ_Standard.MODID, "advancement");
	
    @Override
    public ResourceLocation getRegistryName()
    {
        return REG_ID;
    }
    
    @Override
    public TaskAdvancement createNew()
    {
        return new TaskAdvancement();
    }
    
    @Override
    public TaskAdvancement loadFromData(NBTTagCompound nbt)
    {
        TaskAdvancement task = new TaskAdvancement();
        task.readFromNBT(nbt);
        return task;
    }
}
