package bq_standard.importers.ftbq;

import betterquesting.api.placeholders.ItemPlaceholder;
import betterquesting.api.utils.BigItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

public class FTBQUtils
{
    public static BigItemStack convertItem(NBTBase tag)
    {
        if(tag == null)
        {
            return new BigItemStack(ItemStack.EMPTY);
        } else if(tag.getId() == 8)
        {
            return convertItemType1(((NBTTagString)tag).getString());
        } else if(tag.getId() == 10)
        {
            return convertItemType2((NBTTagCompound)tag);
        }
        
        return new BigItemStack(ItemStack.EMPTY);
    }
    
    private static BigItemStack convertItemType1(String string)
    {
        String[] split = string.split(" ");
        if(split.length <= 0) new BigItemStack(ItemStack.EMPTY);
        
        Item item = Item.REGISTRY.getObject(new ResourceLocation(split[0]));
        int count = split.length < 2 ? 0 : Integer.parseInt(split[1]);
        int meta = split.length < 3 ? 0 : Integer.parseInt(split[2]);
        NBTTagCompound tags = null;
        
        if(item == null)
        {
            tags = new NBTTagCompound();
            tags.setString("orig_id", split[0]);
            item = ItemPlaceholder.placeholder;
        }
        
        BigItemStack stack = new BigItemStack(item, count, meta);
        if(tags != null) stack.SetTagCompound(tags);
        return stack;
    }
    
    private static BigItemStack convertItemType2(NBTTagCompound tag)
    {
        String[] split = tag.getString("id").split(" ");
        if(split.length <= 0) new BigItemStack(ItemStack.EMPTY);
        
        Item item = Item.REGISTRY.getObject(new ResourceLocation(split[0]));
        int count = split.length < 2 ? 0 : Integer.parseInt(split[1]);
        int meta = split.length < 3 ? 0 : Integer.parseInt(split[2]);
        NBTTagCompound tags;
        
        if(item == null)
        {
            tags = new NBTTagCompound();
            tags.setString("orig_id", split[0]);
            item = ItemPlaceholder.placeholder;
        } else
        {
            tags = tag.getCompoundTag("tag");
        }
        
        BigItemStack stack = new BigItemStack(item, count, meta);
        stack.SetTagCompound(tags);
        return stack;
    }
}
