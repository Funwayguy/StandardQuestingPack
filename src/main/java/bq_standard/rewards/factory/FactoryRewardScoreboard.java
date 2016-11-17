package bq_standard.rewards.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.rewards.IReward;
import betterquesting.api.utils.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.RewardScoreboard;
import com.google.gson.JsonObject;

public class FactoryRewardScoreboard implements IFactory<IReward>
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
	public IReward createNew()
	{
		return new RewardScoreboard();
	}

	@Override
	public IReward loadFromJson(JsonObject json)
	{
		RewardScoreboard reward = new RewardScoreboard();
		reward.readFromJson(json, EnumSaveType.CONFIG);
		return reward;
	}
	
}
