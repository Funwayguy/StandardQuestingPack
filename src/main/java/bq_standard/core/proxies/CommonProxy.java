package bq_standard.core.proxies;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketRegistry;
import betterquesting.api.questing.rewards.IRewardRegistry;
import betterquesting.api.questing.tasks.ITaskRegistry;
import bq_standard.core.BQ_Standard;
import bq_standard.handlers.EventHandler;
import bq_standard.network.handlers.*;
import bq_standard.rewards.factory.*;
import bq_standard.rewards.loot.LootRegistry;
import bq_standard.tasks.factory.*;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		MinecraftForge.EVENT_BUS.register(new LootRegistry());
		MinecraftForge.EVENT_BUS.register(new EventHandler());
	}
	
	public void registerRenderers()
	{
	}
	
	public void registerExpansion()
	{
		ITaskRegistry taskReg = QuestingAPI.getAPI(ApiReference.TASK_REG);
		taskReg.registerTask(FactoryTaskBlockBreak.INSTANCE);
		taskReg.registerTask(FactoryTaskCheckbox.INSTANCE);
		taskReg.registerTask(FactoryTaskCrafting.INSTANCE);
		taskReg.registerTask(FactoryTaskFluid.INSTANCE);
		taskReg.registerTask(FactoryTaskHunt.INSTANCE);
		taskReg.registerTask(FactoryTaskLocation.INSTANCE);
		taskReg.registerTask(FactoryTaskMeeting.INSTANCE);
		taskReg.registerTask(FactoryTaskRetrieval.INSTANCE);
		taskReg.registerTask(FactoryTaskScoreboard.INSTANCE);
		taskReg.registerTask(FactoryTaskXP.INSTANCE);
		taskReg.registerTask(FactoryTaskAdvancement.INSTANCE);

		IRewardRegistry rewardReg = QuestingAPI.getAPI(ApiReference.REWARD_REG);
		rewardReg.registerReward(FactoryRewardChoice.INSTANCE);
		rewardReg.registerReward(FactoryRewardCommand.INSTANCE);
		rewardReg.registerReward(FactoryRewardItem.INSTANCE);
		rewardReg.registerReward(FactoryRewardScoreboard.INSTANCE);
		rewardReg.registerReward(FactoryRewardXP.INSTANCE);
		
		IPacketRegistry packetReg = QuestingAPI.getAPI(ApiReference.PACKET_REG);
		packetReg.registerHandler(new PktHandlerLootDatabase());
		packetReg.registerHandler(new PktHandlerCheckbox());
		packetReg.registerHandler(new PktHandlerScoreboard());
		packetReg.registerHandler(new PktHandlerChoice());
		packetReg.registerHandler(new PktHandlerLootImport());
		
		BQ_Standard.lootChest.setCreativeTab(QuestingAPI.getAPI(ApiReference.CREATIVE_TAB));
	}
}
