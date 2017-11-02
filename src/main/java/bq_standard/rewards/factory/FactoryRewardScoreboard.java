package bq_standard.rewards.factory;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.RewardScoreboard;

public class FactoryRewardScoreboard implements IFactory<RewardScoreboard>
{
	public static final FactoryRewardScoreboard INSTANCE = new FactoryRewardScoreboard();
	
	private FactoryRewardScoreboard()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID, "scoreboard");
	}

	@Override
	public RewardScoreboard createNew()
	{
		return new RewardScoreboard();
	}

	@Override
	public RewardScoreboard loadFromNBT(NBTTagCompound json)
	{
		RewardScoreboard reward = new RewardScoreboard();
		reward.readFromNBT(json, EnumSaveType.CONFIG);
		return reward;
	}
	
}
