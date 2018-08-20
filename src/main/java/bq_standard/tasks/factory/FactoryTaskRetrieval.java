package bq_standard.tasks.factory;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskRetrieval;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public final class FactoryTaskRetrieval implements IFactory<TaskRetrieval>
{
	public static final FactoryTaskRetrieval INSTANCE = new FactoryTaskRetrieval();
	
	private FactoryTaskRetrieval()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID + ":retrieval");
	}

	@Override
	public TaskRetrieval createNew()
	{
		return new TaskRetrieval();
	}

	@Override
	public TaskRetrieval loadFromNBT(NBTTagCompound json)
	{
		TaskRetrieval task = new TaskRetrieval();
		task.readFromNBT(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
