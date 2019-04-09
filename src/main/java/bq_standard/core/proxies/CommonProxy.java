package bq_standard.core.proxies;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.IPacketRegistry;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.registry.IFactoryData;
import betterquesting.api2.registry.IRegistry;
import bq_standard.core.BQ_Standard;
import bq_standard.handlers.EventHandler;
import bq_standard.network.handlers.*;
import bq_standard.rewards.factory.*;
import bq_standard.rewards.loot.LootRegistry;
import bq_standard.tasks.factory.*;
import net.minecraft.nbt.NBTTagCompound;
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
		IRegistry<IFactoryData<ITask, NBTTagCompound>, ITask> taskReg = QuestingAPI.getAPI(ApiReference.TASK_REG);
		taskReg.register(FactoryTaskBlockBreak.INSTANCE);
		taskReg.register(FactoryTaskCheckbox.INSTANCE);
		taskReg.register(FactoryTaskCrafting.INSTANCE);
		taskReg.register(FactoryTaskFluid.INSTANCE);
		taskReg.register(FactoryTaskHunt.INSTANCE);
		taskReg.register(FactoryTaskLocation.INSTANCE);
		taskReg.register(FactoryTaskMeeting.INSTANCE);
		taskReg.register(FactoryTaskRetrieval.INSTANCE);
		taskReg.register(FactoryTaskScoreboard.INSTANCE);
		taskReg.register(FactoryTaskXP.INSTANCE);
		taskReg.register(FactoryTaskAdvancement.INSTANCE);
		taskReg.register(FactoryTaskTame.INSTANCE);
		taskReg.register(FactoryTaskInteractItem.INSTANCE);
		taskReg.register(FactoryTaskInteractEntity.INSTANCE);
		taskReg.register(FactoryTaskTrigger.INSTANCE);

		IRegistry<IFactoryData<IReward, NBTTagCompound>, IReward> rewardReg = QuestingAPI.getAPI(ApiReference.REWARD_REG);
		rewardReg.register(FactoryRewardChoice.INSTANCE);
		rewardReg.register(FactoryRewardCommand.INSTANCE);
		rewardReg.register(FactoryRewardItem.INSTANCE);
		rewardReg.register(FactoryRewardScoreboard.INSTANCE);
		rewardReg.register(FactoryRewardXP.INSTANCE);
		rewardReg.register(FactoryRewardRecipe.INSTANCE);
		
		IPacketRegistry packetReg = QuestingAPI.getAPI(ApiReference.PACKET_REG);
		packetReg.registerHandler(new PktHandlerLootDatabase());
		packetReg.registerHandler(new PktHandlerCheckbox());
		packetReg.registerHandler(new PktHandlerScoreboard());
		packetReg.registerHandler(new PktHandlerChoice());
		packetReg.registerHandler(new PktHandlerLootImport());
		packetReg.registerHandler(new PktHandlerInteract());
		
		BQ_Standard.lootChest.setCreativeTab(QuestingAPI.getAPI(ApiReference.CREATIVE_TAB));
	}
}
