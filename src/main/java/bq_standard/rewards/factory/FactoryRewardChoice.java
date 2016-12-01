package bq_standard.rewards.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.RewardChoice;
import com.google.gson.JsonObject;

public class FactoryRewardChoice implements IFactory<RewardChoice>
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
	public RewardChoice createNew()
	{
		return new RewardChoice();
	}

	@Override
	public RewardChoice loadFromJson(JsonObject json)
	{
		RewardChoice reward = new RewardChoice();
		reward.readFromJson(json, EnumSaveType.CONFIG);
		return reward;
	}
	
}
