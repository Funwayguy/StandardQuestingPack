package bq_standard.tasks.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskRetrieval;
import com.google.gson.JsonObject;

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
	public TaskRetrieval loadFromJson(JsonObject json)
	{
		TaskRetrieval task = new TaskRetrieval();
		task.readFromJson(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
