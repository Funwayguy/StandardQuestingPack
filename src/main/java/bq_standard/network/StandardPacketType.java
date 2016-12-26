package bq_standard.network;

import net.minecraft.util.ResourceLocation;
import bq_standard.core.BQ_Standard;

public enum StandardPacketType
{
	LOOT_SYNC,
	LOOT_CLAIM,
	CHECKBOX,
	SCORE_SYNC,
	CHOICE;
	
	private final ResourceLocation ID;
	
	private StandardPacketType()
	{
		ID = new ResourceLocation(BQ_Standard.MODID, this.toString().toLowerCase());
	}
	
	public ResourceLocation GetLocation()
	{
		return ID;
	}
	
	public String GetName()
	{
		return ID.toString();
	}
}
