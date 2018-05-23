package bq_standard.client.gui.rewards;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.RenderUtils;
import bq_standard.client.gui.GuiScrollingItemsSmall;
import bq_standard.network.StandardPacketType;
import bq_standard.rewards.RewardChoice;

public class GuiRewardChoice extends GuiElement implements IGuiEmbedded
{
	private RewardChoice reward;
	private Minecraft mc;
	
	private GuiScrollingItemsSmall itemScroll;
	private int posX = 0;
	private int posY = 0;
	private int sizeY = 0;
	
	private int qID = -1;
	private int rID = -1;
	
	public GuiRewardChoice(RewardChoice reward, IQuest quest, int posX, int posY, int sizeX, int sizeY)
	{
		this.mc = Minecraft.getMinecraft();
		this.reward = reward;
		this.posX = posX;
		this.posY = posY;
		this.sizeY = sizeY;
		
		this.qID = QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest);
		this.rID = quest.getRewards().getID(reward);
		
		this.itemScroll = new GuiScrollingItemsSmall(mc, posX + 40, posY, sizeX - 40, sizeY);
		
		for(BigItemStack stack : reward.choices)
		{
			this.itemScroll.addItem(new BigItemStack(stack.getBaseStack()), stack.stackSize + " " + stack.getBaseStack().getDisplayName());
		}
	}

	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		int sel = reward.getSelecton(QuestingAPI.getQuestingUUID(mc.player));
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(posX, posY + sizeY/2 - 18, 0);
		GlStateManager.scale(2F, 2F, 1F);
		this.mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		this.drawTexturedModalRect(0, 0, 0, 48, 18, 18);
		GlStateManager.enableDepth();
		
		if(sel >= 0)
		{
			BigItemStack selStack = reward.choices.get(sel);
			RenderUtils.RenderItemStack(mc, selStack.getBaseStack(), 1, 1, selStack.stackSize > 0? "" + selStack.stackSize : "");
		}
		
		GlStateManager.disableDepth();
		GlStateManager.popMatrix();
		
		itemScroll.drawBackground(mx, my, partialTick);
	}
	
	@Override
	public void drawForeground(int mx, int my, float partialTick)
	{
		itemScroll.drawForeground(mx, my, partialTick);
	}
	
	@Override
	public void onMouseClick(int mx, int my, int button)
	{
		int idx = itemScroll.getEntryUnderMouse(mx, my);
		
		if(idx >= 0)
		{
			NBTTagCompound retTags = new NBTTagCompound();
			retTags.setInteger("questID", qID);
			retTags.setInteger("rewardID", rID);
			retTags.setInteger("selection", idx);
			QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(StandardPacketType.CHOICE.GetLocation(), retTags));
		}
	}

	@Override
	public void onMouseScroll(int mx, int my, int scroll)
	{
	}

	@Override
	public void onKeyTyped(char c, int keyCode)
	{
	}
}
