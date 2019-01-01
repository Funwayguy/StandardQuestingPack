package bq_standard.tasks.factory;

import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskCrafting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public final class FactoryTaskCrafting implements IFactory<TaskCrafting>
{
	public static final FactoryTaskCrafting INSTANCE = new FactoryTaskCrafting();
	
	private FactoryTaskCrafting()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID + ":crafting");
	}

	@Override
	public TaskCrafting createNew()
	{
		return new TaskCrafting();
	}

	@Override
	public TaskCrafting loadFromNBT(NBTTagCompound json)
	{
		TaskCrafting task = new TaskCrafting();
		task.readFromNBT(json);
		return task;
	}
	
}
