package bq_standard.importers.hqm.converters.items;

import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.utils.BigItemStack;

public class HQMItemHeart extends HQMItem
{
	private final Item bqHeart;
	
	public HQMItemHeart()
	{
		bqHeart = (Item)Item.REGISTRY.getObject(new ResourceLocation("betterquesting:extra_life"));
	}
	
	@Override
	public BigItemStack convertItem(int damage, int amount, NBTTagCompound tags)
	{
		int amt = amount;
		int dmg = 0;
		
		switch(damage)
		{
			case 0:
				dmg = 2;
				break;
			case 1:
				dmg = 1;
				break;
			case 2:
				dmg = 2;
				amt *= 3;
				break;
			default:
				dmg = 0;
				break;
		}
		
		return new BigItemStack(bqHeart, amt, dmg);
	}
}
