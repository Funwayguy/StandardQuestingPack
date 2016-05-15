package bq_standard.importers;

import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.editors.explorer.FileExtentionFilter;
import betterquesting.client.gui.editors.explorer.GuiFileExplorer;
import betterquesting.client.gui.editors.explorer.IFileCallback;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.importers.ImporterBase;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import bq_standard.client.gui.importers.GuiNativeFileImporter;
import bq_standard.core.BQ_Standard;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class NativeFileImporter extends ImporterBase implements IFileCallback
{
	public static NativeFileImporter instance = new NativeFileImporter();
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.importer.nat_file.name";
	}
	
	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiNativeFileImporter(screen, posX, posY, sizeX, sizeY);
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
		
		// Store all the old data somewhere while we use the built in loaders
		ConcurrentHashMap<Integer,QuestInstance> oldQuests = QuestDatabase.questDB;
		QuestDatabase.questDB.clear();;
		CopyOnWriteArrayList<QuestLine> oldLines = QuestDatabase.questLines;
		QuestDatabase.questLines.clear();
		
		// Use native parsing to ensure it is always up to date
		QuestDatabase.readFromJson(json);
		
		QuestDatabase.questLines.addAll(oldLines);
		ConcurrentHashMap<Integer,QuestInstance> tmp = oldQuests;
		oldQuests = QuestDatabase.questDB;
		QuestDatabase.questDB = tmp;
		
		for(QuestInstance q : oldQuests.values())
		{
			int id = QuestDatabase.getUniqueID();
			q.questID = id;
			QuestDatabase.questDB.put(id, q);
		}
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
		
		QuestDatabase.UpdateClients();
	}
}
