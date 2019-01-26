package bq_standard.rewards.factory;

import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.RewardCommand;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryRewardCommand implements IFactory<RewardCommand>
{
	public static final FactoryRewardCommand INSTANCE = new FactoryRewardCommand();
	
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
		reward.readFromNBT(json);
		return reward;
	}
	
}
