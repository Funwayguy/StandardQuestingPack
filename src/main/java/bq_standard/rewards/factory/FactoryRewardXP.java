package bq_standard.rewards.factory;

import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.RewardXP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FactoryRewardXP implements IFactory<RewardXP>
{
	public static final FactoryRewardXP INSTANCE = new FactoryRewardXP();
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID, "xp");
	}

	@Override
	public RewardXP createNew()
	{
		return new RewardXP();
	}

	@Override
	public RewardXP loadFromNBT(NBTTagCompound json)
	{
		RewardXP reward = new RewardXP();
		reward.readFromNBT(json);
		return reward;
	}
	
}
