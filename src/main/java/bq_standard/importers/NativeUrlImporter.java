package bq_standard.importers;

import org.apache.logging.log4j.Level;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.importers.ImporterBase;
import bq_standard.client.gui.UpdateNotification;
import bq_standard.client.gui.importers.GuiNativeUrlImporter;
import bq_standard.core.BQ_Standard;

public class NativeUrlImporter extends ImporterBase
{
	public static NativeUrlImporter instance = new NativeUrlImporter();
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.importer.nat_url.name";
	}
	
	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiNativeUrlImporter(screen, posX, posY, sizeX, sizeY);
	}
	
	public static boolean startImport(String url)
	{
		try
		{
			String rawJson = UpdateNotification.getNotification(url, true);
			JsonObject json = new Gson().fromJson(rawJson, JsonObject.class);
			NativeFileImporter.ImportQuestLine(json);
			return true;
		} catch(Exception e)
		{
			BQ_Standard.logger.log(Level.INFO, "Unable to import quest database from URL: " + url, e);
			return false; // TODO: Use notification download, parse with GSON, send to standard importer
		}
	}
}
