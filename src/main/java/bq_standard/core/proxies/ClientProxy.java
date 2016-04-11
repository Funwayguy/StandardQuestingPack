package bq_standard.core.proxies;

import java.awt.Color;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.client.themes.ThemeStandard;
import betterquesting.importers.ImporterRegistry;
import bq_standard.core.BQ_Standard;
import bq_standard.importers.NativeFileImporter;
import bq_standard.importers.NativeUrlImporter;
import bq_standard.importers.hqm.HQMBagImporter;
import bq_standard.importers.hqm.HQMQuestImporter;
import bq_standard.network.PacketStandard;

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
		
    	BQ_Standard.instance.network.registerMessage(PacketStandard.HandlerClient.class, PacketStandard.class, 0, Side.CLIENT);
    	
    	if(Loader.isModLoaded("JustEnoughItems"))
    	{
    		//NEIRegister.instance.registerHandler();
    	}
    	
    	ImporterRegistry.registerImporter(HQMQuestImporter.instance);
    	ImporterRegistry.registerImporter(HQMBagImporter.instance);
    	ImporterRegistry.registerImporter(NativeFileImporter.instance);
    	ImporterRegistry.registerImporter(NativeUrlImporter.instance);
	}
	
	@Override
	public void registerThemes()
	{
		ThemeRegistry.RegisterTheme(new ThemeStandard("Standard Light", new ResourceLocation("betterquesting", "textures/gui/editor_gui.png")), "light");
		ThemeRegistry.RegisterTheme(new ThemeStandard("Standard Dark", new ResourceLocation("bq_standard", "textures/gui/editor_gui_dark.png")).setTextColor(Color.WHITE), "dark");
		ThemeRegistry.RegisterTheme(new ThemeStandard("Stronghold", new ResourceLocation("bq_standard", "textures/gui/editor_gui_stronghold.png")).setTextColor(Color.WHITE), "stronghold");
		ThemeRegistry.RegisterTheme(new ThemeStandard("Overworld", new ResourceLocation("bq_standard", "textures/gui/editor_gui_overworld.png")).setTextColor(Color.WHITE), "overworld");
		ThemeRegistry.RegisterTheme(new ThemeStandard("Nether", new ResourceLocation("bq_standard", "textures/gui/editor_gui_nether.png")).setTextColor(Color.WHITE), "nether");
		ThemeRegistry.RegisterTheme(new ThemeStandard("End", new ResourceLocation("bq_standard", "textures/gui/editor_gui_end.png")).setTextColor(Color.WHITE), "end");
		ThemeRegistry.RegisterTheme(new ThemeStandard("Vanilla", new ResourceLocation("bq_standard", "textures/gui/editor_gui_vanilla.png")), "vanilla");
	}
	
	@Override
	public void registerRenderers()
	{
		super.registerRenderers();
		
		registerItemModelSubtypes(BQ_Standard.lootChest, 0, 102, BQ_Standard.lootChest.getRegistryName().toString());
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerBlockModel(Block block)
	{
		registerBlockModel(block, 0, block.getRegistryName().toString());
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerBlockModel(Block block, int meta, String name)
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
	public static void registerItemModelSubtypes(Item item, int metaStart, int metaEnd, String name)
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
	public static void registerItemModel(Item item)
	{
		registerItemModel(item, 0, item.getRegistryName().toString());
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerItemModel(Item item, int meta, String name)
	{
		ModelResourceLocation model = new ModelResourceLocation(name, "inventory");
		
		if(!name.equals(item.getRegistryName()))
		{
		    ModelBakery.registerItemVariants(item, model);
		}
		
	    Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta, model);
	}
}
