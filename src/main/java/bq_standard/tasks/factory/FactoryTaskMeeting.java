package bq_standard.tasks.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.tasks.ITask;
import betterquesting.api.utils.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskFluid;
import com.google.gson.JsonObject;

public final class FactoryTaskMeeting implements IFactory<ITask>
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
	public ITask createNew()
	{
		return new TaskFluid();
	}

	@Override
	public ITask loadFromJson(JsonObject json)
	{
		TaskFluid task = new TaskFluid();
		task.readFromJson(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
