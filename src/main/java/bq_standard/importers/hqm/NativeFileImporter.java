package bq_standard.importers.hqm;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class NativeFileImporter extends ImporterBase
{
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
		JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Import Native Quest Database");
		fc.setCurrentDirectory(new File("."));
		fc.setMultiSelectionEnabled(true);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Quest Database", "json");
		fc.setFileFilter(filter);
		
		if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
		{
			File[] selList = fc.getSelectedFiles();
			
			for(File selected : selList)
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
		}
	}

	private static void ImportQuestLine(JsonObject json)
	{
		if(json == null)
		{
			return;
		}
		
		// Store all the old data somewhere while we use the built in loaders
		HashMap<Integer,QuestInstance> oldQuests = QuestDatabase.questDB;
		QuestDatabase.questDB = new HashMap<Integer,QuestInstance>();
		ArrayList<QuestLine> oldLines = QuestDatabase.questLines;
		QuestDatabase.questLines = new ArrayList<QuestLine>();
		
		// Use native parsing to ensure it is always up to date
		QuestDatabase.readFromJson(json);
		
		QuestDatabase.questLines.addAll(oldLines);
		HashMap<Integer,QuestInstance> tmp = oldQuests;
		oldQuests = QuestDatabase.questDB;
		QuestDatabase.questDB = tmp;
		
		for(QuestInstance q : oldQuests.values())
		{
			int id = QuestDatabase.getUniqueID();
			q.questID = id;
			QuestDatabase.questDB.put(id, q);
		}
	}
}
