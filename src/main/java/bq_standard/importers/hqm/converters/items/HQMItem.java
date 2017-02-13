package bq_standard.importers.hqm.converters.items;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.utils.BigItemStack;

public interface HQMItem
{
	public BigItemStack convertItem(int damage, int amount, NBTTagCompound tags);
}
