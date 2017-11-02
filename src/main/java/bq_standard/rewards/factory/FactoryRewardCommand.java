package bq_standard.rewards.factory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.RewardCommand;

public class FactoryRewardCommand implements IFactory<RewardCommand>
{
	public static final FactoryRewardCommand INSTANCE = new FactoryRewardCommand();
	
	private FactoryRewardCommand()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID, "command");
	}

	@Override
	public RewardCommand createNew()
	{
		return new RewardCommand();
	}

	@Override
	public RewardCommand loadFromNBT(NBTTagCompound json)
	{
		RewardCommand reward = new RewardCommand();
		reward.readFromNBT(json, EnumSaveType.CONFIG);
		return reward;
	}
	
}
