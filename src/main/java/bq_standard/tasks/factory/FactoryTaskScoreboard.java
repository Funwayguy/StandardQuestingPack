package bq_standard.tasks.factory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskScoreboard;

public final class FactoryTaskScoreboard implements IFactory<TaskScoreboard>
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
	public TaskScoreboard createNew()
	{
		return new TaskScoreboard();
	}

	@Override
	public TaskScoreboard loadFromNBT(NBTTagCompound json)
	{
		TaskScoreboard task = new TaskScoreboard();
		task.readFromNBT(json, EnumSaveType.CONFIG);
		return task;
	}
	
}
