package bq_standard.rewards;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import bq_standard.NBTReplaceUtil;
import bq_standard.client.gui.rewards.GuiRewardItem;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.factory.FactoryRewardItem;

public class RewardItem implements IReward
{
	public ArrayList<BigItemStack> items = new ArrayList<BigItemStack>();
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryRewardItem.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.reward.item";
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, IQuest quest)
	{
		return true;
	}

	@Override
	public void claimReward(EntityPlayer player, IQuest quest)
	{
		for(BigItemStack r : items)
		{
			BigItemStack stack = r.copy();
			
			for(ItemStack s : stack.getCombinedStacks())
			{
				if(s.getTagCompound() != null)
				{
					s.setTagCompound(NBTReplaceUtil.replaceStrings(s.getTagCompound(), "VAR_NAME", player.getName()));
					s.setTagCompound(NBTReplaceUtil.replaceStrings(s.getTagCompound(), "VAR_UUID", QuestingAPI.getQuestingUUID(player).toString()));
				}
				
				if(!player.inventory.addItemStackToInventory(s))
				{
					player.dropItem(s, true, false);
				}
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		items = new ArrayList<BigItemStack>();
		NBTTagList rList = json.getTagList("rewards", 10);
		for(int i = 0; i < rList.tagCount(); i++)
		{
			NBTBase entry = rList.get(i);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			try
			{
				BigItemStack item = JsonHelper.JsonToItemStack((NBTTagCompound)entry);
				
				if(item != null)
				{
					items.add(item);
				} else
				{
					continue;
				}
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load reward item data", e);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		NBTTagList rJson = new NBTTagList();
		for(BigItemStack stack : items)
		{
			rJson.appendTag(JsonHelper.ItemStackToJson(stack, new NBTTagCompound()));
		}
		json.setTag("rewards", rJson);
		return json;
	}

	@Override
	public IGuiEmbedded getRewardGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new GuiRewardItem(this, posX, posY, sizeX, sizeY);
	}
	
	@Override
	public GuiScreen getRewardEditor(GuiScreen screen, IQuest quest)
	{
		return null;
	}

	@Override
	public IJsonDoc getDocumentation()
	{
		return null;
	}
}
