package bq_standard.network.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import bq_standard.network.StandardPacketType;
import bq_standard.rewards.RewardChoice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PktHandlerChoice implements IPacketHandler
{
	
	@Override
	public ResourceLocation getRegistryName()
	{
		return StandardPacketType.CHOICE.GetLocation();
	}
	
	@Override
	public void handleServer(NBTTagCompound tag, EntityPlayerMP sender)
	{
		if(sender == null)
		{
			return;
		}
		
		int qID = tag.hasKey("questID")? tag.getInteger("questID") : -1;
		int rID = tag.hasKey("rewardID")? tag.getInteger("rewardID") : -1;
		int sel = tag.hasKey("selection")? tag.getInteger("selection") : -1;
		
		if(qID < 0 || rID < 0)
		{
			return;
		}
		
		IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(qID);
		IReward reward = quest == null? null : quest.getRewards().getValue(rID);
		
		if(reward instanceof RewardChoice)
		{
			RewardChoice rChoice = (RewardChoice)reward;
			rChoice.setSelection(QuestingAPI.getQuestingUUID(sender), sel);
			
			NBTTagCompound retTags = new NBTTagCompound();
			retTags.setInteger("questID", qID);
			retTags.setInteger("rewardID", rID);
			retTags.setInteger("selection", sel);
			QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToPlayer(new QuestingPacket(StandardPacketType.CHOICE.GetLocation(), retTags), sender);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void handleClient(NBTTagCompound tag)
	{
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		
		int qID = tag.hasKey("questID")? tag.getInteger("questID") : -1;
		int rID = tag.hasKey("rewardID")? tag.getInteger("rewardID") : -1;
		int sel = tag.hasKey("selection")? tag.getInteger("selection") : -1;
		
		if(qID < 0 || rID < 0)
		{
			return;
		}
		
		IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(qID);
		IReward reward = quest == null? null : quest.getRewards().getValue(rID);
		
		if(reward != null && reward instanceof RewardChoice)
		{
			RewardChoice rChoice = (RewardChoice)reward;
			rChoice.setSelection(QuestingAPI.getQuestingUUID(player), sel);
		}
	}
}
