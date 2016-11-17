package bq_standard.importers;

import net.minecraft.client.gui.GuiScreen;
import org.apache.logging.log4j.Level;
import betterquesting.api.ExpansionAPI;
import betterquesting.api.client.io.IQuestIO;
import betterquesting.api.network.PreparedPayload;
import betterquesting.database.QuestDatabase;
import bq_standard.client.gui.UpdateNotification;
import bq_standard.client.gui.importers.GuiNativeUrlImporter;
import bq_standard.core.BQ_Standard;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class NativeUrlImporter implements IQuestIO
{
	public static NativeUrlImporter instance = new NativeUrlImporter();
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.importer.nat_url.name";
	}
	
	@Override
	public String getUnlocalisedDescrition()
	{
		return "bq_standard.importer.nat_url.desc";
	}
	
	@Override
	public GuiScreen openGui(GuiScreen screen)
	{
		return null;//new GuiNativeUrlImporter(screen, posX, posY, sizeX, sizeY);
	}
	
	public static boolean startImport(String url)
	{
		try
		{
			String rawJson = UpdateNotification.getNotification(url, true);
			JsonObject json = new Gson().fromJson(rawJson, JsonObject.class);
			
			NativeFileImporter.ImportQuestLine(json);
			
			PreparedPayload pp = QuestDatabase.INSTANCE.getSyncPacket();
			ExpansionAPI.getAPI().getPacketSender().sendToServer(pp);
			
			return true;
		} catch(Exception e)
		{
			BQ_Standard.logger.log(Level.INFO, "Unable to import quest database from URL: " + url, e);
			return false;
		}
	}
}
