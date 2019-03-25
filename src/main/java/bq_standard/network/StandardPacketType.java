package bq_standard.network;

import bq_standard.core.BQ_Standard;
import net.minecraft.util.ResourceLocation;

public enum StandardPacketType
{
	LOOT_SYNC,
	LOOT_CLAIM,
	LOOT_IMPORT,
	CHECKBOX,
	SCORE_SYNC,
	CHOICE,
    INTERACT;
	
	private final ResourceLocation ID;
	
	StandardPacketType()
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
