package bq_standard.tasks.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskLocation;
import com.google.gson.JsonObject;

public final class FactoryTaskLocation implements IFactory<TaskLocation>
{
	public static final FactoryTaskLocation INSTANCE = new FactoryTaskLocation();
	
	private FactoryTaskLocation()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID + ":location");
	}

	@Override
	public TaskLocation createNew()
	{
		return new TaskLocation();
	}

	@Override
	public TaskLocation loadFromJson(JsonObject json)
	{
		TaskLocation task = new TaskLocation();
		task.readFromJson(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
