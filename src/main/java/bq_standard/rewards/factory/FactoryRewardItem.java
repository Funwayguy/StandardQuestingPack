package bq_standard.rewards.factory;

import betterquesting.api.misc.IFactory;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.RewardItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

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
	public RewardItem loadFromNBT(NBTTagCompound json)
	{
		RewardItem reward = new RewardItem();
		reward.readFromNBT(json);
		return reward;
	}
	
}
