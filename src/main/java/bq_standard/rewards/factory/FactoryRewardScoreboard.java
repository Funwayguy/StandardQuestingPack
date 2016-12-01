package bq_standard.rewards.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.RewardScoreboard;
import com.google.gson.JsonObject;

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
	public RewardScoreboard loadFromJson(JsonObject json)
	{
		RewardScoreboard reward = new RewardScoreboard();
		reward.readFromJson(json, EnumSaveType.CONFIG);
		return reward;
	}
	
}
