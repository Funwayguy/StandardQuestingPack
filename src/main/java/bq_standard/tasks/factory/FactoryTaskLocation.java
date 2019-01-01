package bq_standard.tasks.factory;

import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskLocation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public final class FactoryTaskLocation implements IFactory<TaskLocation>
{
	public static final FactoryTaskLocation INSTANCE = new FactoryTaskLocation();
	
	private FactoryTaskLocation()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID + ":location");
	}

	@Override
	public TaskLocation createNew()
	{
		return new TaskLocation();
	}

	@Override
	public TaskLocation loadFromNBT(NBTTagCompound json)
	{
		TaskLocation task = new TaskLocation();
		task.readFromNBT(json);
		return task;
	}
	
}
