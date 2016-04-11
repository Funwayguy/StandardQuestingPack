package bq_standard.rewards;

import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.JsonHelper;
import bq_standard.NBTReplaceUtil;
import bq_standard.client.gui.rewards.GuiRewardChoice;
import bq_standard.core.BQ_Standard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RewardChoice extends RewardBase
{
	public int selected = -1;
	public ArrayList<BigItemStack> choices = new ArrayList<BigItemStack>();
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.reward.choice";
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, NBTTagCompound choiceData)
	{
		selected = choiceData.hasKey("selected")? choiceData.getInteger("selected") : -1;
		return choices.size() <= 0 || (selected >= 0 && selected < choices.size());
	}

	@Override
	public void Claim(EntityPlayer player, NBTTagCompound choiceData)
	{
		if(choices.size() <= 0)
		{
			return;
		}
		
		selected = choiceData.hasKey("selected")? choiceData.getInteger("selected") : -1;
		
		if(selected < 0 || selected >= choices.size())
		{
			BQ_Standard.logger.log(Level.ERROR, "Choice reward was forcibly claimed with invalid choice", new IllegalStateException());
			return;
		}
		
		BigItemStack stack = choices.get(selected);
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
				s.setTagCompound(NBTReplaceUtil.replaceStrings(s.getTagCompound(), "VAR_UUID", player.getUniqueID().toString()));
			}
			
			if(!player.inventory.addItemStackToInventory(s))
			{
				player.dropPlayerItemWithRandomChoice(s, false);
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public NBTTagCompound GetChoiceData()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("selected", selected);
		return tag;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void SetChoiceData(NBTTagCompound tags)
	{
		selected = tags.hasKey("selected")? tags.getInteger("selected") : -1;
	}
	
	@Override
	public void readFromJson(JsonObject json)
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
	public void writeToJson(JsonObject json)
	{
		JsonArray rJson = new JsonArray();
		for(BigItemStack stack : choices)
		{
			rJson.add(JsonHelper.ItemStackToJson(stack, new JsonObject()));
		}
		json.add("choices", rJson);
	}

	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiRewardChoice(this, screen, posX, posY, sizeX, sizeY);
	}
}
