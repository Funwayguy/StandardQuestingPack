package bq_standard.client.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.ItemComparison;
import bq_standard.rewards.RewardChoice;
import bq_standard.rewards.RewardItem;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.api.IRecipeOverlayRenderer;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.ICraftingHandler;

public class NEIRewardHandler implements ICraftingHandler
{
	ArrayList<RewardInfo> infoList = new ArrayList<RewardInfo>();
	
	public NEIRewardHandler(){}
	
	public NEIRewardHandler(ArrayList<RewardInfo> info)
	{
		this.infoList = info;
	}
	
	@Override
	public String getRecipeName()
	{
		return "Questing Reward";
	}
	
	@Override
	public int numRecipes()
	{
		return infoList.size();
	}
	
	@Override
	public void drawBackground(int recipe)
	{
		Minecraft mc = Minecraft.getMinecraft();
		
		if(recipe < 0 || recipe >= infoList.size())
		{
			return;
		}
		
        RewardInfo i = infoList.get(recipe);

        mc.fontRenderer.drawString("x" + i.stack.stackSize, 166/2, 14, Color.GRAY.getRGB());
        mc.fontRenderer.drawString(i.quest, 166/2 - mc.fontRenderer.getStringWidth(i.quest)/2, 30, Color.BLACK.getRGB());
        mc.fontRenderer.drawString(i.reward, 166/2 - mc.fontRenderer.getStringWidth(i.reward)/2, 40, Color.BLACK.getRGB());
	}
	
	@Override
	public void drawForeground(int recipe)
	{
	}
	
	@Override
	public List<PositionedStack> getIngredientStacks(int recipe)
	{
		return new ArrayList<PositionedStack>();
	}
	
	@Override
	public List<PositionedStack> getOtherStacks(int recipetype)
	{
		return new ArrayList<PositionedStack>();
	}
	
	@Override
	public PositionedStack getResultStack(int recipe)
	{
		if(recipe < 0 || recipe >= infoList.size())
		{
			return null;
		}
		
		return new PositionedStack(infoList.get(recipe).stack.getBaseStack(), 166/2 - 18, 10);
	}
	
	@Override
	public void onUpdate()
	{
	}
	
	@Override
	public boolean hasOverlay(GuiContainer gui, Container container, int recipe)
	{
		return false;
	}
	
	@Override
	public IRecipeOverlayRenderer getOverlayRenderer(GuiContainer gui, int recipe)
	{
		return null;
	}
	
	@Override
	public IOverlayHandler getOverlayHandler(GuiContainer gui, int recipe)
	{
		return null;
	}
	
	@Override
	public int recipiesPerPage()
	{
		return 2;
	}
	
	@Override
	public List<String> handleTooltip(GuiRecipe gui, List<String> currenttip, int recipe)
	{
		return currenttip;
	}
	
	@Override
	public List<String> handleItemTooltip(GuiRecipe gui, ItemStack stack, List<String> currenttip, int recipe)
	{
		return currenttip;
	}
	
	@Override
	public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe)
	{
		return false;
	}
	
	@Override
	public boolean mouseClicked(GuiRecipe gui, int button, int recipe)
	{
		return false;
	}
	
	@Override
	public ICraftingHandler getRecipeHandler(String outputId, Object... results)
	{
		if(outputId == null || !outputId.equalsIgnoreCase("item"))
		{
			return this;
		}
		
		ArrayList<RewardInfo> tmp = new ArrayList<RewardInfo>();
		
		for(Object o : results)
		{
			if(!(o instanceof ItemStack))
			{
				continue;
			}
			
			ItemStack stack = (ItemStack)o;
			
			qloop:
			for(QuestInstance q : QuestDatabase.questDB.values())
			{
				for(RewardBase r : q.rewards)
				{
					if(r instanceof RewardItem)
					{
						RewardItem ri = (RewardItem)r;
						
						for(BigItemStack rStack : ri.items)
						{
							if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, false, true))
							{
								tmp.add(new RewardInfo(rStack, q, r));
								continue qloop;
							}
						}
					} else if(r instanceof RewardChoice)
					{
						RewardChoice rc = (RewardChoice)r;
						
						for(BigItemStack rStack : rc.choices)
						{
							if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, false, true))
							{
								tmp.add(new RewardInfo(rStack, q, r));
								continue qloop;
							}
						}
					}
				}
			}
		}
		
		if(tmp.size() <= 0)
		{
			return this;
		} else
		{
			return new NEIRewardHandler(tmp);
		}
	}
	
	public static class RewardInfo
	{
		BigItemStack stack;
		String quest;
		String reward;
		
		public RewardInfo(BigItemStack stack, QuestInstance quest, RewardBase reward)
		{
			this.stack = stack;
			this.quest = I18n.format(quest.name);
			this.reward = reward.getDisplayName();
			
			boolean flag = false;
			for(QuestLine ql : QuestDatabase.questLines)
			{
				if(ql.getQuests().contains(quest))
				{
					flag = true;
					break;
				}
			}
			
			if(!flag)
			{
				this.quest = "???";
			}
		}
	}
}
