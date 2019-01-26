package bq_standard.tasks.factory;

import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskFluid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskFluid implements IFactory<TaskFluid>
{
	public static final FactoryTaskFluid INSTANCE = new FactoryTaskFluid();
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID + ":fluid");
	}

	@Override
	public TaskFluid createNew()
	{
		return new TaskFluid();
	}

	@Override
	public TaskFluid loadFromNBT(NBTTagCompound json)
	{
		TaskFluid task = new TaskFluid();
		task.readFromNBT(json);
		return task;
	}
	
}
