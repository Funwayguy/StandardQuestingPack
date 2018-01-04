package bq_standard.tasks.factory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskCheckbox;

public final class FactoryTaskCheckbox implements IFactory<TaskCheckbox>
{
	public static final FactoryTaskCheckbox INSTANCE = new FactoryTaskCheckbox();
	
	private FactoryTaskCheckbox()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID + ":checkbox");
	}

	@Override
	public TaskCheckbox createNew()
	{
		return new TaskCheckbox();
	}

	@Override
	public TaskCheckbox loadFromNBT(NBTTagCompound json)
	{
		TaskCheckbox task = new TaskCheckbox();
		task.readFromNBT(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
