package bq_standard.importers.hqm;

import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.importers.ImporterBase;

public class NativeUrlImporter extends ImporterBase
{
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.importer.nat_url.name";
	}
	
	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return null;
	}
	
	/**
	 * WIP, Importers currently don't support text fields
	 */
	public static boolean startImport(String url)
	{
		return false;
	}
}
