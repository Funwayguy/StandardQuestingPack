package bq_standard.rewards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.rewards.IReward;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import bq_standard.NBTReplaceUtil;
import bq_standard.client.gui.rewards.GuiRewardChoice;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.factory.FactoryRewardChoice;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
		if(!selected.containsKey(player.getGameProfile().getId()))
		{
			return false;
		}
		
		int tmp = selected.get(player.getGameProfile().getId());
		return choices.size() <= 0 || (tmp >= 0 && tmp < choices.size());
	}

	@Override
	public void claimReward(EntityPlayer player, IQuest quest)
	{
		if(choices.size() <= 0)
		{
			return;
		} else if(!selected.containsKey(player.getGameProfile().getId()))
		{
			return;
		}
		
		int tmp = selected.get(player.getGameProfile().getId());
		
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
				s.setTagCompound(NBTReplaceUtil.replaceStrings(s.getTagCompound(), "VAR_NAME", player.getCommandSenderName()));
				s.setTagCompound(NBTReplaceUtil.replaceStrings(s.getTagCompound(), "VAR_UUID", player.getGameProfile().getId().toString()));
			}
			
			if(!player.inventory.addItemStackToInventory(s))
			{
				player.dropPlayerItemWithRandomChoice(s, false);
			}
		}
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		choices = new ArrayList<BigItemStack>();
		for(JsonElement entry : JsonHelper.GetArray(json, "choices"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			BigItemStack item = JsonHelper.JsonToItemStack(entry.getAsJsonObject());
			
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
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		JsonArray rJson = new JsonArray();
		for(BigItemStack stack : choices)
		{
			rJson.add(JsonHelper.ItemStackToJson(stack, new JsonObject()));
		}
		json.add("choices", rJson);
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
}
