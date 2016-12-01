package bq_standard.tasks.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskCrafting;
import com.google.gson.JsonObject;

public final class FactoryTaskCrafting implements IFactory<TaskCrafting>
{
	public static final FactoryTaskCrafting INSTANCE = new FactoryTaskCrafting();
	
	private FactoryTaskCrafting()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID + ":crafting");
	}

	@Override
	public TaskCrafting createNew()
	{
		return new TaskCrafting();
	}

	@Override
	public TaskCrafting loadFromJson(JsonObject json)
	{
		TaskCrafting task = new TaskCrafting();
		task.readFromJson(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
