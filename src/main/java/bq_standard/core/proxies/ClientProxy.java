package bq_standard.core.proxies;

import java.awt.Color;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.themes.IThemeRegistry;
import bq_standard.client.theme.ThemeStandard;
import bq_standard.importers.NativeFileImporter;
import bq_standard.importers.hqm.HQMBagImporter;
import bq_standard.importers.hqm.HQMQuestImporter;
import bq_standard.nei.NEIRegister;
import bq_standard.network.handlers.PktHandlerLootClaim;
import cpw.mods.fml.common.Loader;

public class ClientProxy extends CommonProxy
{
	@Override
	public boolean isClient()
	{
		return true;
	}
	
	@Override
	public void registerHandlers()
	{
		super.registerHandlers();
    	
    	if(Loader.isModLoaded("NotEnoughItems"))
    	{
    		NEIRegister.instance.registerHandler();
    	}
	}
	
	@Override
	public void registerExpansion()
	{
		super.registerExpansion();
		
		QuestingAPI.getAPI(ApiReference.PACKET_REG).registerHandler(new PktHandlerLootClaim());
		
		QuestingAPI.getAPI(ApiReference.IMPORT_REG).registerImporter(new NativeFileImporter());
		QuestingAPI.getAPI(ApiReference.IMPORT_REG).registerImporter(new HQMQuestImporter());
		QuestingAPI.getAPI(ApiReference.IMPORT_REG).registerImporter(new HQMBagImporter());
		
		IThemeRegistry themeReg = QuestingAPI.getAPI(ApiReference.THEME_REG);
		themeReg.registerTheme(new ThemeStandard("Standard Light", new ResourceLocation("betterquesting", "textures/gui/editor_gui.png"), new ResourceLocation("bq_standard", "light")));
		themeReg.registerTheme(new ThemeStandard("Standard Dark", new ResourceLocation("bq_standard", "textures/gui/editor_gui_dark.png"), new ResourceLocation("bq_standard", "dark")).setTextColor(Color.WHITE.getRGB()));
		themeReg.registerTheme(new ThemeStandard("Stronghold", new ResourceLocation("bq_standard", "textures/gui/editor_gui_stronghold.png"), new ResourceLocation("bq_standard", "stronghold")).setTextColor(Color.WHITE.getRGB()));
		themeReg.registerTheme(new ThemeStandard("Overworld", new ResourceLocation("bq_standard", "textures/gui/editor_gui_overworld.png"), new ResourceLocation("bq_standard", "overworld")).setTextColor(Color.WHITE.getRGB()));
		themeReg.registerTheme(new ThemeStandard("Nether", new ResourceLocation("bq_standard", "textures/gui/editor_gui_nether.png"), new ResourceLocation("bq_standard", "nether")).setTextColor(Color.WHITE.getRGB()));
		themeReg.registerTheme(new ThemeStandard("End", new ResourceLocation("bq_standard", "textures/gui/editor_gui_end.png"), new ResourceLocation("bq_standard", "end")).setTextColor(Color.WHITE.getRGB()));
		themeReg.registerTheme(new ThemeStandard("Vanilla", new ResourceLocation("bq_standard", "textures/gui/editor_gui_vanilla.png"), new ResourceLocation("bq_standard", "vanilla")));
	}
}
