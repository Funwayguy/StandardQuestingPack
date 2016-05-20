package bq_standard.network;

import net.minecraft.util.ResourceLocation;
import betterquesting.core.BetterQuesting;
import bq_standard.core.BQ_Standard;

public enum StandardPacketType
{
	LOOT_SYNC,
	LOOT_CLAIM,
	CHECKBOX,
	SCORE_SYNC;
	
	public ResourceLocation GetLocation()
	{
		return new ResourceLocation(BQ_Standard.MODID + ":" + this.toString().toLowerCase());
	}
	
	public String GetName()
	{
		return BetterQuesting.MODID + ":" + this.toString().toLowerCase();
	}
}
