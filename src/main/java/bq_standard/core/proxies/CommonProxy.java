package bq_standard.core.proxies;

import net.minecraftforge.common.MinecraftForge;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketRegistry;
import betterquesting.api.questing.rewards.IRewardRegistry;
import betterquesting.api.questing.tasks.ITaskRegistry;
import bq_standard.client.gui.UpdateNotification;
import bq_standard.handlers.EventHandler;
import bq_standard.network.handlers.PktHandlerCheckbox;
import bq_standard.network.handlers.PktHandlerChoice;
import bq_standard.network.handlers.PktHandlerLootDatabase;
import bq_standard.network.handlers.PktHandlerScoreboard;
import bq_standard.rewards.factory.FactoryRewardChoice;
import bq_standard.rewards.factory.FactoryRewardCommand;
import bq_standard.rewards.factory.FactoryRewardItem;
import bq_standard.rewards.factory.FactoryRewardScoreboard;
import bq_standard.rewards.factory.FactoryRewardXP;
import bq_standard.rewards.loot.LootRegistry;
import bq_standard.tasks.factory.FactoryTaskBlockBreak;
import bq_standard.tasks.factory.FactoryTaskCheckbox;
import bq_standard.tasks.factory.FactoryTaskCrafting;
import bq_standard.tasks.factory.FactoryTaskFluid;
import bq_standard.tasks.factory.FactoryTaskHunt;
import bq_standard.tasks.factory.FactoryTaskLocation;
import bq_standard.tasks.factory.FactoryTaskMeeting;
import bq_standard.tasks.factory.FactoryTaskRetrieval;
import bq_standard.tasks.factory.FactoryTaskScoreboard;
import bq_standard.tasks.factory.FactoryTaskXP;
import cpw.mods.fml.common.FMLCommonHandler;

public class CommonProxy
{
	public boolean isClient()
	{
		return false;
	}
	
	public void registerHandlers()
	{
		FMLCommonHandler.instance().bus().register(new UpdateNotification());
		MinecraftForge.EVENT_BUS.register(new LootRegistry());
		EventHandler eh = new EventHandler();
		MinecraftForge.EVENT_BUS.register(eh);
		FMLCommonHandler.instance().bus().register(eh);
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
	}
}
