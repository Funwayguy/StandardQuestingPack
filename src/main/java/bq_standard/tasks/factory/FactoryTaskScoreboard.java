package bq_standard.tasks.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.other.IFactory;
import betterquesting.api.questing.tasks.ITask;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskScoreboard;
import com.google.gson.JsonObject;

public final class FactoryTaskScoreboard implements IFactory<ITask>
{
	public static final FactoryTaskScoreboard INSTANCE = new FactoryTaskScoreboard();
	
	private FactoryTaskScoreboard()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID + ":scoreboard");
	}

	@Override
	public ITask createNew()
	{
		return new TaskScoreboard();
	}

	@Override
	public ITask loadFromJson(JsonObject json)
	{
		TaskScoreboard task = new TaskScoreboard();
		task.readFromJson(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
