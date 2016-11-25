package bq_standard.tasks.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import betterquesting.api.questing.tasks.ITask;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskBlockBreak;
import com.google.gson.JsonObject;

public final class FactoryTaskBlockBreak implements IFactory<ITask>
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
	public TaskBlockBreak loadFromJson(JsonObject json)
	{
		TaskBlockBreak task = new TaskBlockBreak();
		task.readFromJson(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
