package bq_standard.tasks.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskMeeting;
import com.google.gson.JsonObject;

public final class FactoryTaskMeeting implements IFactory<TaskMeeting>
{
	public static final FactoryTaskMeeting INSTANCE = new FactoryTaskMeeting();
	
	private FactoryTaskMeeting()
	{
	}
	
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
	public TaskMeeting loadFromJson(JsonObject json)
	{
		TaskMeeting task = new TaskMeeting();
		task.readFromJson(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
