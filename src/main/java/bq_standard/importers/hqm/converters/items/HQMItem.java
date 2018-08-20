package bq_standard.importers.hqm.converters.items;

import betterquesting.api.utils.BigItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface HQMItem
{
	public BigItemStack convertItem(int damage, int amount, NBTTagCompound tags);
}
