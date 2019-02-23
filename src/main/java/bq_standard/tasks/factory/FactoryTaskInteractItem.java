package bq_standard.tasks.factory;

import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskInteractItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskInteractItem implements IFactory<TaskInteractItem>
{
    public static final FactoryTaskInteractItem INSTANCE = new FactoryTaskInteractItem();
    
	private final ResourceLocation REG_ID = new ResourceLocation(BQ_Standard.MODID, "interact_item");
	
    @Override
    public ResourceLocation getRegistryName()
    {
        return REG_ID;
    }
    
    @Override
    public TaskInteractItem createNew()
    {
        return new TaskInteractItem();
    }
    
    @Override
    public TaskInteractItem loadFromNBT(NBTTagCompound nbt)
    {
        TaskInteractItem task = new TaskInteractItem();
        task.readFromNBT(nbt);
        return task;
    }
}
