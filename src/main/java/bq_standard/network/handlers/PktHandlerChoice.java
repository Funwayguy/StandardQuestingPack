package bq_standard.network.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.ExpansionAPI;
import betterquesting.api.network.IPacketHandler;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.rewards.IReward;
import bq_standard.network.StandardPacketType;
import bq_standard.rewards.RewardChoice;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
		
		IQuest quest = ExpansionAPI.getAPI().getQuestDB().getValue(qID);
		IReward reward = quest == null? null : quest.getRewards().getValue(rID);
		
		if(reward != null && reward instanceof RewardChoice)
		{
			RewardChoice rChoice = (RewardChoice)reward;
			rChoice.setSelection(ExpansionAPI.getAPI().getNameCache().getQuestingID(sender), sel);
			
			NBTTagCompound retTags = new NBTTagCompound();
			retTags.setInteger("questID", qID);
			retTags.setInteger("rewardID", rID);
			retTags.setInteger("selection", sel);
			ExpansionAPI.getAPI().getPacketSender().sendToPlayer(new PreparedPayload(StandardPacketType.CHOICE.GetLocation(), retTags), sender);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void handleClient(NBTTagCompound tag)
	{
		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		
		int qID = tag.hasKey("questID")? tag.getInteger("questID") : -1;
		int rID = tag.hasKey("rewardID")? tag.getInteger("rewardID") : -1;
		int sel = tag.hasKey("selection")? tag.getInteger("selection") : -1;
		
		if(qID < 0 || rID < 0)
		{
			return;
		}
		
		IQuest quest = ExpansionAPI.getAPI().getQuestDB().getValue(qID);
		IReward reward = quest == null? null : quest.getRewards().getValue(rID);
		
		if(reward != null && reward instanceof RewardChoice)
		{
			RewardChoice rChoice = (RewardChoice)reward;
			rChoice.setSelection(ExpansionAPI.getAPI().getNameCache().getQuestingID(player), sel);
		}
	}
}
