package bq_standard.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
import bq_standard.client.gui.rewards.GuiRewardChoice;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.factory.FactoryRewardChoice;

public class RewardChoice implements IReward
{
	/**
	 * The selected reward index to be claimed.<br>
	 * Should only ever be used client side. NEVER on server
	 */
	public ArrayList<BigItemStack> choices = new ArrayList<BigItemStack>();
	private HashMap<UUID,Integer> selected = new HashMap<UUID,Integer>();
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryRewardChoice.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.reward.choice";
	}
	
	public int getSelecton(UUID uuid)
	{
		if(!selected.containsKey(uuid))
		{
			return -1;
		}
		
		return selected.get(uuid);
	}
	
	public void setSelection(UUID uuid, int value)
	{
		selected.put(uuid, value);
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, IQuest quest)
	{
		if(!selected.containsKey(QuestingAPI.getQuestingUUID(player)))
		{
			return false;
		}
		
		int tmp = selected.get(QuestingAPI.getQuestingUUID(player));
		return choices.size() <= 0 || (tmp >= 0 && tmp < choices.size());
	}

	@Override
	public void claimReward(EntityPlayer player, IQuest quest)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		if(choices.size() <= 0)
		{
			return;
		} else if(!selected.containsKey(playerID))
		{
			return;
		}
		
		int tmp = selected.get(playerID);
		
		if(tmp < 0 || tmp >= choices.size())
		{
			BQ_Standard.logger.log(Level.ERROR, "Choice reward was forcibly claimed with invalid choice", new IllegalStateException());
			return;
		}
		
		BigItemStack stack = choices.get(tmp);
		stack = stack == null? null : stack.copy();
		
		if(stack == null || stack.stackSize <= 0)
		{
			BQ_Standard.logger.log(Level.WARN, "Claimed reward choice was null or was 0 in size!");
			return;
		}
		
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
	
	@Override
	public void readFromNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		choices = new ArrayList<BigItemStack>();
		NBTTagList cList = json.getTagList("choices", 10);
		for(int i = 0; i < cList.tagCount(); i++)
		{
			NBTBase entry = cList.get(i);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			BigItemStack item = JsonHelper.JsonToItemStack((NBTTagCompound)entry);
			
			if(item != null)
			{
				choices.add(item);
			} else
			{
				continue;
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		NBTTagList rJson = new NBTTagList();
		for(BigItemStack stack : choices)
		{
			rJson.appendTag(JsonHelper.ItemStackToJson(stack, new NBTTagCompound()));
		}
		json.setTag("choices", rJson);
		return json;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiEmbedded getRewardGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new GuiRewardChoice(this, quest, posX, posY, sizeX, sizeY);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
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
