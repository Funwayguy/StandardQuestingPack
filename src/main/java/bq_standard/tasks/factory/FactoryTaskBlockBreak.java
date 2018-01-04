package bq_standard.tasks.factory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskBlockBreak;

public final class FactoryTaskBlockBreak implements IFactory<TaskBlockBreak>
{
	public static final FactoryTaskBlockBreak INSTANCE = new FactoryTaskBlockBreak();
	
	private FactoryTaskBlockBreak()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID, "block_break");
	}

	@Override
	public TaskBlockBreak createNew()
	{
		return new TaskBlockBreak();
	}

	@Override
	public TaskBlockBreak loadFromNBT(NBTTagCompound json)
	{
		TaskBlockBreak task = new TaskBlockBreak();
		task.readFromNBT(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
