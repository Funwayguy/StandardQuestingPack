package bq_standard.tasks.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import betterquesting.api.questing.tasks.ITask;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskCheckbox;
import com.google.gson.JsonObject;

public final class FactoryTaskCheckbox implements IFactory<ITask>
{
	public static final FactoryTaskCheckbox INSTANCE = new FactoryTaskCheckbox();
	
	private FactoryTaskCheckbox()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID + ":checkbox");
	}

	@Override
	public TaskCheckbox createNew()
	{
		return new TaskCheckbox();
	}

	@Override
	public TaskCheckbox loadFromJson(JsonObject json)
	{
		TaskCheckbox task = new TaskCheckbox();
		task.readFromJson(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
