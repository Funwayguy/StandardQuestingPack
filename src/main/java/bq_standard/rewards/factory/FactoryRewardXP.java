package bq_standard.rewards.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.rewards.IReward;
import betterquesting.api.utils.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.RewardXP;
import com.google.gson.JsonObject;

public class FactoryRewardXP implements IFactory<IReward>
{
	public static final FactoryRewardXP INSTANCE = new FactoryRewardXP();
	
	private FactoryRewardXP()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID, "xp");
	}

	@Override
	public IReward createNew()
	{
		return new RewardXP();
	}

	@Override
	public IReward loadFromJson(JsonObject json)
	{
		RewardXP reward = new RewardXP();
		reward.readFromJson(json, EnumSaveType.CONFIG);
		return reward;
	}
	
}
