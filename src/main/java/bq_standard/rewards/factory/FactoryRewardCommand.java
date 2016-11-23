package bq_standard.rewards.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.other.IFactory;
import betterquesting.api.questing.rewards.IReward;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.RewardCommand;
import com.google.gson.JsonObject;

public class FactoryRewardCommand implements IFactory<IReward>
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
	public IReward createNew()
	{
		return new RewardCommand();
	}

	@Override
	public IReward loadFromJson(JsonObject json)
	{
		RewardCommand reward = new RewardCommand();
		reward.readFromJson(json, EnumSaveType.CONFIG);
		return reward;
	}
	
}
