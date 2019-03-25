package bq_standard.items;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.utils.BigItemStack;
import bq_standard.core.BQ_Standard;
import bq_standard.network.StandardPacketType;
import bq_standard.rewards.loot.LootGroup;
import bq_standard.rewards.loot.LootRegistry;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.util.List;

public class ItemLootChest extends Item
{
	public ItemLootChest()
	{
		this.setMaxStackSize(1);
		this.setUnlocalizedName("bq_standard.loot_chest");
		this.setCreativeTab(QuestingAPI.getAPI(ApiReference.CREATIVE_TAB));
	}

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
	@Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
    {
		if(hand != EnumHand.MAIN_HAND)
		{
			return new ActionResult<>(EnumActionResult.PASS, stack);
		}
		
    	if(stack.getItemDamage() >= 102)
    	{
    		if(QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(player))
    		{
    			player.openGui(BQ_Standard.instance, 0, world, (int)player.posX, (int)player.posY, (int)player.posZ);
    		}
			return new ActionResult<>(EnumActionResult.PASS, stack);
    	} else if(!world.isRemote)
    	{
    		LootGroup group;
    		if(stack.getItemDamage() == 101)
    		{
    			group = LootRegistry.INSTANCE.getWeightedGroup(itemRand.nextFloat(), itemRand);
    		} else
    		{
    			group = LootRegistry.INSTANCE.getWeightedGroup(MathHelper.clamp_int(stack.getItemDamage(), 0, 100)/100F, itemRand);
    		}
	    	List<BigItemStack> loot;
	    	String title = "Dungeon Loot";
	    	
	    	if(group == null)
	    	{
	    		loot = LootRegistry.getStandardLoot(player);
	    	} else
	    	{
	    		title = group.name;
	    		loot = group.getRandomReward(itemRand);
	    		
	    		if(loot == null || loot.size() <= 0)
	    		{
	    			BQ_Standard.logger.log(Level.WARN, "Unable to get random loot entry from group " + group.name + "! Reason: Contains 0 loot entries");
	    			title = "Dungeon Loot";
	    			loot = LootRegistry.getStandardLoot(player);
	    		}
	    	}
	    	
	    	for(BigItemStack s1 : loot)
	    	{
	    		for(ItemStack s2 : s1.getCombinedStacks())
	    		{
		    		if(!player.inventory.addItemStackToInventory(s2))
		    		{
		    			player.dropItem(s2, true, false);
		    		}
	    		}
	    		
	    		player.inventory.markDirty();
	    		player.inventoryContainer.detectAndSendChanges();
	    	}
	    	
	    	if(player instanceof EntityPlayerMP)
	    	{
	    		sendGui((EntityPlayerMP)player, loot, title);
	    	}
    	}
    	
    	if(!player.capabilities.isCreativeMode)
    	{
    		stack.stackSize--;
    	}
    	
    	return new ActionResult<>(EnumActionResult.PASS, stack);
    }
	
	private void sendGui(EntityPlayerMP player, List<BigItemStack> loot, String title)
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setString("title", title);
		
		NBTTagList list = new NBTTagList();
		
		for(BigItemStack stack : loot)
		{
			if(stack == null)
			{
				continue;
			}
			
			list.appendTag(stack.writeToNBT(new NBTTagCompound()));
		}
		
		tags.setTag("rewards", list);
		
		QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToPlayer(new QuestingPacket(StandardPacketType.LOOT_CLAIM.GetLocation(), tags), player);
	}

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
	@Override
	@SideOnly(Side.CLIENT)
    public void getSubItems(@Nonnull Item item, CreativeTabs tab, List<ItemStack> list)
    {
    	if(tab == CreativeTabs.SEARCH || tab == this.getCreativeTab())
		{
			list.add(new ItemStack(this, 1, 0));
			list.add(new ItemStack(this, 1, 25));
			list.add(new ItemStack(this, 1, 50));
			list.add(new ItemStack(this, 1, 75));
			list.add(new ItemStack(this, 1, 100));
			list.add(new ItemStack(this, 1, 101));
			list.add(new ItemStack(this, 1, 102));
		}
    }
	
	@Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack)
    {
        return stack.getItemDamage() > 101;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
    {
		if(stack.getItemDamage() > 101)
		{
			tooltip.add(I18n.format("betterquesting.btn.edit"));
		} else if(QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE))
		{
			if(stack.getItemDamage() == 101)
			{
				tooltip.add(I18n.format("bq_standard.tooltip.loot_chest", "???"));
			} else
			{
				tooltip.add(I18n.format("bq_standard.tooltip.loot_chest", MathHelper.clamp_int(stack.getItemDamage(), 0, 100) + "%"));
			}
		}
    }
}
