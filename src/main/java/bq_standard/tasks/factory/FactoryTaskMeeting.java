package bq_standard.tasks.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskFluid;
import com.google.gson.JsonObject;

public final class FactoryTaskMeeting implements IFactory<TaskFluid>
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
	public TaskFluid createNew()
	{
		return new TaskFluid();
	}

	@Override
	public TaskFluid loadFromJson(JsonObject json)
	{
		TaskFluid task = new TaskFluid();
		task.readFromJson(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
