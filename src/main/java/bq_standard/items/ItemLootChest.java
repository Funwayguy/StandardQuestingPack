package bq_standard.items;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import betterquesting.api.ExpansionAPI;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.quests.properties.NativeProps;
import betterquesting.api.utils.BigItemStack;
import betterquesting.core.BetterQuesting;
import betterquesting.quests.QuestSettings;
import bq_standard.core.BQ_Standard;
import bq_standard.network.StandardPacketType;
import bq_standard.rewards.loot.LootGroup;
import bq_standard.rewards.loot.LootRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemLootChest extends Item
{
	public ItemLootChest()
	{
		this.setMaxStackSize(1);
		this.setUnlocalizedName("bq_standard.loot_chest");
		this.setTextureName("bq_standard:loot_chest");
		this.setCreativeTab(BetterQuesting.tabQuesting);
	}

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
    	if(stack.getItemDamage() >= 102)
    	{
    		if(QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE))
    		{
    			player.openGui(BQ_Standard.instance, 0, world, (int)player.posX, (int)player.posY, (int)player.posZ);
    		}
			return stack;
    	} else if(!world.isRemote)
    	{
    		LootGroup group;
    		if(stack.getItemDamage() == 101)
    		{
    			group = LootRegistry.getWeightedGroup(itemRand.nextFloat(), itemRand);
    		} else
    		{
    			group = LootRegistry.getWeightedGroup(MathHelper.clamp_int(stack.getItemDamage(), 0, 100)/100F, itemRand);
    		}
	    	ArrayList<BigItemStack> loot = new ArrayList<BigItemStack>();
	    	String title = "Dungeon Loot";
	    	
	    	if(group == null)
	    	{
	    		loot = LootRegistry.getStandardLoot(itemRand);
	    	} else
	    	{
	    		title = group.name;
	    		loot = group.getRandomReward(itemRand);
	    		
	    		if(loot == null || loot.size() <= 0)
	    		{
	    			BQ_Standard.logger.log(Level.WARN, "Unable to get random loot entry from group " + group.name + "! Reason: Contains 0 loot entries");
	    			title = "Dungeon Loot";
	    			loot = LootRegistry.getStandardLoot(itemRand);
	    		}
	    	}
	    	
	    	for(BigItemStack s1 : loot)
	    	{
	    		for(ItemStack s2 : s1.getCombinedStacks())
	    		{
		    		if(!player.inventory.addItemStackToInventory(s2))
		    		{
		    			player.dropPlayerItemWithRandomChoice(s2, false);
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
    	
    	return stack;
    }
	
	public void sendGui(EntityPlayerMP player, ArrayList<BigItemStack> loot, String title)
	{
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", 0);
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
		
		ExpansionAPI.getAPI().getPacketSender().sendToPlayer(new PreparedPayload(StandardPacketType.LOOT_CLAIM.GetLocation(), tags), player);
	}

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
	@SideOnly(Side.CLIENT)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void getSubItems(Item item, CreativeTabs tab, List list)
    {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 25));
        list.add(new ItemStack(item, 1, 50));
        list.add(new ItemStack(item, 1, 75));
        list.add(new ItemStack(item, 1, 100));
        list.add(new ItemStack(item, 1, 101));
        list.add(new ItemStack(item, 1, 102));
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
	@SideOnly(Side.CLIENT)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced)
    {
		if(stack.getItemDamage() > 101)
		{
			list.add(StatCollector.translateToLocal("betterquesting.btn.edit"));
		} else if(QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE))
		{
			if(stack.getItemDamage() == 101)
			{
				list.add(StatCollector.translateToLocalFormatted("bq_standard.tooltip.loot_chest", "???"));
			} else
			{
				list.add(StatCollector.translateToLocalFormatted("bq_standard.tooltip.loot_chest", MathHelper.clamp_int(stack.getItemDamage(), 0, 100) + "%"));
			}
		}
    }
}
