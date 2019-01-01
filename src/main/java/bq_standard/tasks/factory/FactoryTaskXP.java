package bq_standard.tasks.factory;

import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskXP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public final class FactoryTaskXP implements IFactory<TaskXP>
{
	public static final FactoryTaskXP INSTANCE = new FactoryTaskXP();
	
	private FactoryTaskXP()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID + ":xp");
	}

	@Override
	public TaskXP createNew()
	{
		return new TaskXP();
	}

	@Override
	public TaskXP loadFromNBT(NBTTagCompound json)
	{
		TaskXP task = new TaskXP();
		task.readFromNBT(json);
		return task;
	}
	
}
