package bq_standard.tasks.factory;

import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskMeeting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryTaskMeeting implements IFactory<TaskMeeting>
{
	public static final FactoryTaskMeeting INSTANCE = new FactoryTaskMeeting();
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID + ":meeting");
	}

	@Override
	public TaskMeeting createNew()
	{
		return new TaskMeeting();
	}

	@Override
	public TaskMeeting loadFromNBT(NBTTagCompound json)
	{
		TaskMeeting task = new TaskMeeting();
		task.readFromNBT(json);
		return task;
	}
	
}
