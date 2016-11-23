package bq_standard.tasks.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.other.IFactory;
import betterquesting.api.questing.tasks.ITask;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskHunt;
import com.google.gson.JsonObject;

public final class FactoryTaskHunt implements IFactory<ITask>
{
	public static final FactoryTaskHunt INSTANCE = new FactoryTaskHunt();
	
	private FactoryTaskHunt()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID + ":hunt");
	}

	@Override
	public ITask createNew()
	{
		return new TaskHunt();
	}

	@Override
	public ITask loadFromJson(JsonObject json)
	{
		TaskHunt task = new TaskHunt();
		task.readFromJson(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
