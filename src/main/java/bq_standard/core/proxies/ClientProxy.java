package bq_standard.core.proxies;

import java.awt.Color;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.themes.IThemeRegistry;
import bq_standard.client.theme.ThemeStandard;
import bq_standard.core.BQ_Standard;
import bq_standard.importers.NativeFileImporter;
import bq_standard.importers.hqm.HQMBagImporter;
import bq_standard.importers.hqm.HQMQuestImporter;
import bq_standard.network.handlers.PktHandlerLootClaim;

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
		
		//MinecraftForge.EVENT_BUS.register(new UpdateNotification());
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
	
	@Override
	public void registerRenderers()
	{
		super.registerRenderers();
		
		registerItemModelSubtypes(BQ_Standard.lootChest, 0, 102, BQ_Standard.lootChest.getRegistryName().toString());
	}
	
	@SideOnly(Side.CLIENT)
	private void registerBlockModel(Block block)
	{
		registerBlockModel(block, 0, block.getRegistryName().toString());
	}
	
	@SideOnly(Side.CLIENT)
	private void registerBlockModel(Block block, int meta, String name)
	{
		Item item = Item.getItemFromBlock(block);
		ModelResourceLocation model = new ModelResourceLocation(name, "inventory");
		
		if(!name.equals(item.getRegistryName()))
		{
		    ModelBakery.registerItemVariants(item, model);
		}
		
	    Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta, model);
	}
	
	@SideOnly(Side.CLIENT)
	private void registerItemModelSubtypes(Item item, int metaStart, int metaEnd, String name)
	{
		if(metaStart > metaEnd)
		{
			int tmp = metaStart;
			metaStart = metaEnd;
			metaEnd = tmp;
		}
		
		for(int m = metaStart; m <= metaEnd; m++)
		{
			registerItemModel(item, m, name);
		}
	}
	
	@SideOnly(Side.CLIENT)
	private void registerItemModel(Item item)
	{
		registerItemModel(item, 0, item.getRegistryName().toString());
	}
	
	@SideOnly(Side.CLIENT)
	private void registerItemModel(Item item, int meta, String name)
	{
		ModelResourceLocation model = new ModelResourceLocation(name, "inventory");
		
		if(!name.equals(item.getRegistryName()))
		{
		    ModelBakery.registerItemVariants(item, model);
		}
		
	    Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta, model);
	}
}
