package bq_standard.importers;

import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.apache.logging.log4j.Level;
import betterquesting.api.ExpansionAPI;
import betterquesting.api.client.IFileCallback;
import betterquesting.api.client.io.IQuestIO;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.utils.FileExtentionFilter;
import betterquesting.client.gui.misc.GuiFileExplorer;
import betterquesting.database.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import bq_standard.client.gui.importers.GuiNativeFileImporter;
import bq_standard.core.BQ_Standard;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class NativeFileImporter implements IQuestIO, IFileCallback
{
	public static NativeFileImporter instance = new NativeFileImporter();
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.importer.nat_file.name";
	}
	
	@Override
	public String getUnlocalisedDescrition()
	{
		return "bq_standard.importer.nat_file.desc";
	}
	
	@Override
	public GuiScreen openGui(GuiScreen screen)
	{
		return null;//new GuiNativeFileImporter(screen, posX, posY, sizeX, sizeY);
	}
	
	@SideOnly(Side.CLIENT)
	public static void StartImport()
	{
		Minecraft mc = Minecraft.getMinecraft();
		mc.displayGuiScreen(new GuiFileExplorer(mc.currentScreen, instance, new File("."), new FileExtentionFilter(".json")));
	}

	protected static void ImportQuestLine(JsonObject json)
	{
		if(json == null)
		{
			return;
		}
		/*
		// Store all the old data somewhere while we use the built in loaders
		ConcurrentHashMap<Integer,QuestInstance> oldQuests = new ConcurrentHashMap<Integer,QuestInstance>();
		oldQuests.putAll(QuestDatabase.questDB);
		QuestDatabase.questDB.clear();
		CopyOnWriteArrayList<QuestLine> oldLines = new CopyOnWriteArrayList<QuestLine>();
		oldLines.addAll(QuestDatabase.questLines);
		QuestDatabase.questLines.clear();
		
		// Use native parsing to ensure it is always up to date
		QuestDatabase.readFromJson(json);
		
		// Merge quest lines
		QuestDatabase.questLines.addAll(oldLines);
		
		// Swap databases in preparation for ID re-mapping
		ConcurrentHashMap<Integer,QuestInstance> tmp = new ConcurrentHashMap<Integer,QuestInstance>();
		tmp.putAll(oldQuests);
		oldQuests.clear();
		oldQuests.putAll(QuestDatabase.questDB);
		QuestDatabase.questDB.clear();
		tmp.putAll(tmp);
		
		// Re-map quest IDs
		for(QuestInstance q : oldQuests.values())
		{
			int id = QuestDatabase.getUniqueID();
			q.questID = id;
			QuestDatabase.questDB.put(id, q);
		}
		*/
	}

	@Override
	public void setFiles(File... files)
	{
		for(File selected : files)
		{
			if(selected == null || !selected.exists())
			{
				continue;
			}
			
			JsonObject json;
			
			try
			{
				FileReader fr = new FileReader(selected);
				Gson g = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
				json = g.fromJson(fr, JsonObject.class);
				fr.close();
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "An error occured during import", e);
				continue;
			}
			
			if(json != null)
			{
				ImportQuestLine(json);
			}
		}
		
		PreparedPayload pp = QuestDatabase.INSTANCE.getSyncPacket();
		ExpansionAPI.getAPI().getPacketSender().sendToServer(pp);
	}
}
