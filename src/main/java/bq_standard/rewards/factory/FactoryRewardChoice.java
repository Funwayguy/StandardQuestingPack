package bq_standard.rewards.factory;

import net.minecraft.util.ResourceLocation;
import com.google.gson.JsonObject;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.rewards.IReward;
import betterquesting.api.utils.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.RewardChoice;

public class FactoryRewardChoice implements IFactory<IReward>
{
	public static final FactoryRewardChoice INSTANCE = new FactoryRewardChoice();
	
	private FactoryRewardChoice()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID, "choice");
	}

	@Override
	public IReward createNew()
	{
		return new RewardChoice();
	}

	@Override
	public IReward loadFromJson(JsonObject json)
	{
		RewardChoice reward = new RewardChoice();
		reward.readFromJson(json, EnumSaveType.CONFIG);
		return reward;
	}
	
}