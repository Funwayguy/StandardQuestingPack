package bq_standard.core;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;
import betterquesting.api.IQuestingAPI;
import betterquesting.api.IQuestingExpansion;
import betterquesting.api.QuestExpansion;
import bq_standard.commands.BQS_Commands;
import bq_standard.core.proxies.CommonProxy;
import bq_standard.handlers.ConfigHandler;
import bq_standard.handlers.GuiHandler;
import bq_standard.items.ItemLootChest;
import bq_standard.network.handlers.PktHandlerCheckbox;
import bq_standard.network.handlers.PktHandlerChoice;
import bq_standard.network.handlers.PktHandlerLootDatabase;
import bq_standard.network.handlers.PktHandlerScoreboard;
import bq_standard.rewards.factory.FactoryRewardChoice;
import bq_standard.rewards.factory.FactoryRewardCommand;
import bq_standard.rewards.factory.FactoryRewardItem;
import bq_standard.rewards.factory.FactoryRewardScoreboard;
import bq_standard.rewards.factory.FactoryRewardXP;
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
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@QuestExpansion
@Mod(modid = BQ_Standard.MODID, version = BQ_Standard.VERSION, name = BQ_Standard.NAME, guiFactory = "bq_standard.handlers.ConfigGuiFactory")
public class BQ_Standard implements IQuestingExpansion
{
    public static final String MODID = "bq_standard";
    public static final String VERSION = "CI_MOD_VERSION";
    public static final String HASH = "CI_MOD_HASH";
    public static final String BRANCH = "CI_MOD_BRANCH";
    public static final String NAME = "Standard Expansion";
    public static final String PROXY = "bq_standard.core.proxies";
    public static final String CHANNEL = "BQ_STANDARD";
	
	@Instance(MODID)
	public static BQ_Standard instance;
	
	@SidedProxy(clientSide = PROXY + ".ClientProxy", serverSide = PROXY + ".CommonProxy")
	public static CommonProxy proxy;
	public SimpleNetworkWrapper network;
	public static Logger logger;
	
	public static Item lootChest = new ItemLootChest();
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	logger = event.getModLog();
    	network = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
    	
    	ConfigHandler.config = new Configuration(event.getSuggestedConfigurationFile(), true);
    	ConfigHandler.initConfigs();
    	
    	proxy.registerHandlers();
    	
    	NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	GameRegistry.registerItem(lootChest, "loot_chest");
    	
    	proxy.registerThemes();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }
	
	@EventHandler
	public void serverStart(FMLServerStartingEvent event)
	{
		MinecraftServer server = MinecraftServer.getServer();
		ICommandManager command = server.getCommandManager();
		ServerCommandManager manager = (ServerCommandManager) command;
		
		manager.registerCommand(new BQS_Commands());
	}

	@Override
	public void registerCommon(IQuestingAPI api)
	{
		api.getTaskRegistry().registerTask(FactoryTaskBlockBreak.INSTANCE);
		api.getTaskRegistry().registerTask(FactoryTaskCheckbox.INSTANCE);
		api.getTaskRegistry().registerTask(FactoryTaskCrafting.INSTANCE);
		api.getTaskRegistry().registerTask(FactoryTaskFluid.INSTANCE);
		api.getTaskRegistry().registerTask(FactoryTaskHunt.INSTANCE);
		api.getTaskRegistry().registerTask(FactoryTaskLocation.INSTANCE);
		api.getTaskRegistry().registerTask(FactoryTaskMeeting.INSTANCE);
		api.getTaskRegistry().registerTask(FactoryTaskRetrieval.INSTANCE);
		api.getTaskRegistry().registerTask(FactoryTaskScoreboard.INSTANCE);
		api.getTaskRegistry().registerTask(FactoryTaskXP.INSTANCE);
		
		api.getRewardRegistry().registerReward(FactoryRewardChoice.INSTANCE);
		api.getRewardRegistry().registerReward(FactoryRewardCommand.INSTANCE);
		api.getRewardRegistry().registerReward(FactoryRewardItem.INSTANCE);
		api.getRewardRegistry().registerReward(FactoryRewardScoreboard.INSTANCE);
		api.getRewardRegistry().registerReward(FactoryRewardXP.INSTANCE);
		
		api.getPacketRegistry().registerHandler(new PktHandlerLootDatabase());
		api.getPacketRegistry().registerHandler(new PktHandlerCheckbox());
		api.getPacketRegistry().registerHandler(new PktHandlerScoreboard());
		api.getPacketRegistry().registerHandler(new PktHandlerChoice());
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerClient(IQuestingAPI api)
	{
	}
}
