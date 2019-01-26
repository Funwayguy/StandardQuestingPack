package bq_standard.tasks.factory;

import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskAdvancement;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public final class FactoryTaskAdvancement implements IFactory<TaskAdvancement>
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
    public TaskAdvancement loadFromNBT(NBTTagCompound nbt)
    {
        TaskAdvancement task = new TaskAdvancement();
        task.readFromNBT(nbt);
        return task;
    }
}
