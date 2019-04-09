package bq_standard.items;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.utils.QuestTranslation;
import bq_standard.core.BQ_Standard;
import bq_standard.network.StandardPacketType;
import bq_standard.rewards.loot.LootGroup;
import bq_standard.rewards.loot.LootRegistry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemLootChest extends Item
{
	public ItemLootChest()
	{
		this.setMaxStackSize(1);
		this.setTranslationKey("bq_standard.loot_chest");
		this.setCreativeTab(QuestingAPI.getAPI(ApiReference.CREATIVE_TAB));
	}

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    @Nonnull
	@Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand)
    {
		ItemStack stack = player.getHeldItem(hand);
		
		if(hand != EnumHand.MAIN_HAND) return new ActionResult<>(EnumActionResult.PASS, stack);
		
		if(stack.getItemDamage() == 103)
        {
            if(world.isRemote || !(player instanceof EntityPlayerMP))
            {
                if(!player.capabilities.isCreativeMode) stack.shrink(1);
    	        return new ActionResult<>(EnumActionResult.PASS, stack);
            }
            
            LootContext lootcontext = (new LootContext.Builder(((EntityPlayerMP)player).getServerWorld())).withLootedEntity(player).withPlayer(player).withLuck(player.getLuck()).build();
            String loottable = (stack.getTagCompound() != null && stack.getTagCompound().hasKey("loottable", 8)) ? stack.getTagCompound().getString("loottable") : "minecraft:chests/simple_dungeon";
            
	    	List<BigItemStack> loot = new ArrayList<>();
            for (ItemStack itemstack : player.world.getLootTableManager().getLootTableFromLocation(new ResourceLocation(loottable)).generateLootForPools(player.getRNG(), lootcontext))
            {
                loot.add(new BigItemStack(itemstack));
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
	    	
            sendGui((EntityPlayerMP)player, loot, "Loot");
        } else if(stack.getItemDamage() >= 102)
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
    			group = LootRegistry.INSTANCE.getWeightedGroup(MathHelper.clamp(stack.getItemDamage(), 0, 100)/100F, itemRand);
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
    	
    	if(!player.capabilities.isCreativeMode) stack.shrink(1);
    	
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
    @SuppressWarnings("rawtypes")
    public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list)
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
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("loottable", "minecraft:chests/simple_dungeon");
			ItemStack lootStack = new ItemStack(this, 1, 103);
			lootStack.setTagCompound(tag);
			list.add(lootStack);
		}
    }
	
	@Override
    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack)
    {
        return stack.getItemDamage() == 102 || stack.getItemDamage() > 103;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        if(stack.getItemDamage() == 103)
        {
            tooltip.add(QuestTranslation.translate("bq_standard.tooltip.loot_table"));
        } else if(stack.getItemDamage() > 101)
		{
			tooltip.add(QuestTranslation.translate("betterquesting.btn.edit"));
		} else if(QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE))
		{
			if(stack.getItemDamage() == 101)
			{
				tooltip.add(QuestTranslation.translate("bq_standard.tooltip.loot_chest", "???"));
			} else
			{
				tooltip.add(QuestTranslation.translate("bq_standard.tooltip.loot_chest", MathHelper.clamp(stack.getItemDamage(), 0, 100) + "%"));
			}
		}
    }
}
