package bq_standard.client.theme;

import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.SimpleTexture;
import betterquesting.api2.client.gui.themes.ThemeRegistry;
import bq_standard.core.BQ_Standard;
import net.minecraft.util.ResourceLocation;

public enum BQSTextures
{
    LOOT_CHEST("loot_chest"),
    LOOT_GLOW("loot_glow");
    
    public static final ResourceLocation TX_LOOT_CHEST = new ResourceLocation(BQ_Standard.MODID, "textures/gui/gui_loot_chest.png");
    
    private final ResourceLocation key;
	
	BQSTextures(String key)
	{
		this.key = new ResourceLocation(BQ_Standard.MODID, key);
	}
	
	public IGuiTexture getTexture()
	{
		return ThemeRegistry.INSTANCE.getTexture(this.key);
	}
	
	public ResourceLocation getKey()
	{
		return this.key;
	}
	
	public static void registerTextures()
    {
        ThemeRegistry.INSTANCE.setDefaultTexture(LOOT_CHEST.key, new SimpleTexture(TX_LOOT_CHEST, new GuiRectangle(0, 0, 128, 68)));
        ThemeRegistry.INSTANCE.setDefaultTexture(LOOT_GLOW.key, new SimpleTexture(TX_LOOT_CHEST, new GuiRectangle(128, 0, 32, 32)));
    }
}
