package bq_standard.core;

import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.Item;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;
import betterquesting.importers.ImporterRegistry;
import betterquesting.quests.rewards.RewardRegistry;
import betterquesting.quests.tasks.TaskRegistry;
import bq_standard.commands.BQS_Commands;
import bq_standard.core.proxies.CommonProxy;
import bq_standard.handlers.ConfigHandler;
import bq_standard.importers.NativeFileImporter;
import bq_standard.importers.NativeUrlImporter;
import bq_standard.importers.hqm.HQMBagImporter;
import bq_standard.importers.hqm.HQMQuestImporter;
import bq_standard.items.ItemLootChest;
import bq_standard.network.GuiHandler;
import bq_standard.rewards.RewardChoice;
import bq_standard.rewards.RewardCommand;
import bq_standard.rewards.RewardItem;
import bq_standard.rewards.RewardScoreboard;
import bq_standard.rewards.RewardXP;
import bq_standard.tasks.TaskBlockBreak;
import bq_standard.tasks.TaskCheckbox;
import bq_standard.tasks.TaskCrafting;
import bq_standard.tasks.TaskFluid;
import bq_standard.tasks.TaskHunt;
import bq_standard.tasks.TaskLocation;
import bq_standard.tasks.TaskMeeting;
import bq_standard.tasks.TaskRetrieval;
import bq_standard.tasks.TaskScoreboard;
import bq_standard.tasks.TaskXP;

@Mod(modid = BQ_Standard.MODID, version = BQ_Standard.VERSION, name = BQ_Standard.NAME, guiFactory = "bq_standard.handlers.ConfigGuiFactory")
public class BQ_Standard
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
    	
    	TaskRegistry.RegisterTask(TaskRetrieval.class, "retrieval");
    	TaskRegistry.RegisterTask(TaskHunt.class, "hunt");
    	TaskRegistry.RegisterTask(TaskLocation.class, "location");
    	TaskRegistry.RegisterTask(TaskCrafting.class, "crafting");
    	TaskRegistry.RegisterTask(TaskScoreboard.class, "scoreboard");
    	TaskRegistry.RegisterTask(TaskFluid.class, "fluid");
    	TaskRegistry.RegisterTask(TaskMeeting.class, "meeting");
    	TaskRegistry.RegisterTask(TaskXP.class, "xp");
    	TaskRegistry.RegisterTask(TaskBlockBreak.class, "block_break");
    	TaskRegistry.RegisterTask(TaskCheckbox.class, "checkbox");
    	
    	RewardRegistry.RegisterReward(RewardItem.class, "item");
    	RewardRegistry.RegisterReward(RewardChoice.class, "choice");
    	RewardRegistry.RegisterReward(RewardScoreboard.class, "scoreboard");
    	RewardRegistry.RegisterReward(RewardCommand.class, "command");
    	RewardRegistry.RegisterReward(RewardXP.class, "xp");
    	
    	ImporterRegistry.registerImporter(new HQMQuestImporter());
    	ImporterRegistry.registerImporter(new HQMBagImporter());
    	ImporterRegistry.registerImporter(new NativeFileImporter());
    	ImporterRegistry.registerImporter(new NativeUrlImporter());
    	
    	proxy.registerRenderers();
    	proxy.registerThemes();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
    }
	
	@EventHandler
	public void serverStart(FMLServerStartingEvent event)
	{
		ICommandManager command = event.getServer().getCommandManager();
		ServerCommandManager manager = (ServerCommandManager) command;
		
		manager.registerCommand(new BQS_Commands());
	}
}
