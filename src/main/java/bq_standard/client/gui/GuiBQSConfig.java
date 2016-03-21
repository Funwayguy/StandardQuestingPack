package bq_standard.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import bq_standard.core.BQ_Standard;
import bq_standard.handlers.ConfigHandler;

@SideOnly(Side.CLIENT)
public class GuiBQSConfig extends GuiConfig
{
	public GuiBQSConfig(GuiScreen parent)
	{
		super(parent, new ConfigElement(ConfigHandler.config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(), BQ_Standard.MODID, false, false, BQ_Standard.NAME);
	}
}
