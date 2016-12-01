package bq_standard.rewards.factory;

import net.minecraft.util.ResourceLocation;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.RewardItem;
import com.google.gson.JsonObject;

public class FactoryRewardItem implements IFactory<RewardItem>
{
	public static final FactoryRewardItem INSTANCE = new FactoryRewardItem();
	
	private FactoryRewardItem()
	{
	}
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return new ResourceLocation(BQ_Standard.MODID, "item");
	}

	@Override
	public RewardItem createNew()
	{
		return new RewardItem();
	}

	@Override
	public RewardItem loadFromJson(JsonObject json)
	{
		RewardItem reward = new RewardItem();
		reward.readFromJson(json, EnumSaveType.CONFIG);
		return reward;
	}
	
}
