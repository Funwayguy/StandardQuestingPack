package bq_standard.tasks.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskXP;
import com.google.gson.JsonObject;

public final class FactoryTaskXP implements IFactory<TaskXP>
{
	public static final FactoryTaskXP INSTANCE = new FactoryTaskXP();
	
	private FactoryTaskXP()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID + ":xp");
	}

	@Override
	public TaskXP createNew()
	{
		return new TaskXP();
	}

	@Override
	public TaskXP loadFromJson(JsonObject json)
	{
		TaskXP task = new TaskXP();
		task.readFromJson(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
