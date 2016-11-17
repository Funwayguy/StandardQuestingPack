package bq_standard.core.proxies;

import java.awt.Color;
import net.minecraft.util.ResourceLocation;
import betterquesting.client.themes.ThemeStandard;
import betterquesting.network.PacketTypeRegistry;
import betterquesting.registry.ThemeRegistry;
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
    	
    	//ImporterRegistry.registerImporter(HQMQuestImporter.instance);
    	//ImporterRegistry.registerImporter(HQMBagImporter.instance);
    	//ImporterRegistry.registerImporter(NativeFileImporter.instance);
    	//ImporterRegistry.registerImporter(NativeUrlImporter.instance);
    	
    	PacketTypeRegistry.INSTANCE.registerHandler(new PktHandlerLootClaim());
	}
	
	@Override
	public void registerThemes()
	{
		ThemeRegistry.INSTANCE.registerTheme(new ThemeStandard("Standard Light", new ResourceLocation("betterquesting", "textures/gui/editor_gui.png"), new ResourceLocation("bq_standard", "light")));
		ThemeRegistry.INSTANCE.registerTheme(new ThemeStandard("Standard Dark", new ResourceLocation("bq_standard", "textures/gui/editor_gui_dark.png"), new ResourceLocation("bq_standard", "dark")).setTextColor(Color.WHITE.getRGB()));
		ThemeRegistry.INSTANCE.registerTheme(new ThemeStandard("Stronghold", new ResourceLocation("bq_standard", "textures/gui/editor_gui_stronghold.png"), new ResourceLocation("bq_standard", "stronghold")).setTextColor(Color.WHITE.getRGB()));
		ThemeRegistry.INSTANCE.registerTheme(new ThemeStandard("Overworld", new ResourceLocation("bq_standard", "textures/gui/editor_gui_overworld.png"), new ResourceLocation("bq_standard", "overworld")).setTextColor(Color.WHITE.getRGB()));
		ThemeRegistry.INSTANCE.registerTheme(new ThemeStandard("Nether", new ResourceLocation("bq_standard", "textures/gui/editor_gui_nether.png"), new ResourceLocation("bq_standard", "nether")).setTextColor(Color.WHITE.getRGB()));
		ThemeRegistry.INSTANCE.registerTheme(new ThemeStandard("End", new ResourceLocation("bq_standard", "textures/gui/editor_gui_end.png"), new ResourceLocation("bq_standard", "end")).setTextColor(Color.WHITE.getRGB()));
		ThemeRegistry.INSTANCE.registerTheme(new ThemeStandard("Vanilla", new ResourceLocation("bq_standard", "textures/gui/editor_gui_vanilla.png"), new ResourceLocation("bq_standard", "vanilla")));
	}
}
