package bq_standard.importers.hqm;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import org.apache.logging.log4j.Level;
import betterquesting.api.importer.IImporter;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.utils.FileExtensionFilter;
import betterquesting.api.utils.JsonHelper;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.loot.LootGroup;
import bq_standard.rewards.loot.LootGroup.LootEntry;
import bq_standard.rewards.loot.LootRegistry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HQMBagImporter implements IImporter
{
	public static HQMBagImporter instance = new HQMBagImporter();
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.importer.hqm_bag.name";
	}
	
	@Override
	public String getUnlocalisedDescription()
	{
		return "bq_standard.importer.hqm_bag.desc";
	}
	
	@Override
	public FileFilter getFileFilter()
	{
		return new FileExtensionFilter(".json");
	}
	
	public static void ImportJsonBags(JsonArray json)
	{
		for(JsonElement e : json)
		{
			if(e == null || !e.isJsonObject())
			{
				continue;
			}
			
			JsonObject jGrp = e.getAsJsonObject();
			
			LootGroup group = new LootGroup();
			group.name = JsonHelper.GetString(jGrp, "name", "HQM Loot");
			try
			{
				int tmp = 0;
				
				JsonArray jWht = JsonHelper.GetArray(jGrp, "weights");
				
				for(int i = 0; i < jWht.size(); i++)
				{
					JsonElement w = jWht.get(i);
					
					if(w == null || !w.isJsonPrimitive() || !w.getAsJsonPrimitive().isNumber())
					{
						continue;
					}
					
					tmp += w.getAsInt() * (jWht.size() - i);
				}
				
				group.weight = Math.max(1, tmp/4);
			} catch(Exception ex)
			{
				group.weight = 1;
			}
			
			for(JsonElement e2 : JsonHelper.GetArray(jGrp, "groups"))
			{
				if(e2 == null || !e2.isJsonObject())
				{
					continue;
				}
				
				JsonObject je = e2.getAsJsonObject();
				LootEntry lEntry = new LootEntry();
				lEntry.weight = JsonHelper.GetNumber(je, "limit", 1).intValue();
				for(JsonElement ji : JsonHelper.GetArray(je, "items"))
				{
					if(ji == null || !ji.isJsonObject())
					{
						continue;
					}
					
					lEntry.items.add(HQMUtilities.HQMStackT1(ji.getAsJsonObject()));
				}
				group.lootEntry.add(lEntry);
			}
			
			LootRegistry.registerGroup(group);
		}
	}

	@Override
	public void loadFiles(IQuestDatabase questDB, IQuestLineDatabase lineDB, File[] files)
	{
		for(File selected : files)
		{
			if(selected == null || !selected.exists())
			{
				continue;
			}
			
			JsonArray json; // Bag.json is formatting as an array!
			
			try
			{
				FileReader fr = new FileReader(selected);
				Gson g = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
				json = g.fromJson(fr, JsonArray.class);
				fr.close();
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "An error occured during import", e);
				continue;
			}
			
			if(json != null)
			{
				ImportJsonBags(json);
			}
		}
		
		LootRegistry.updateClients();
	}
}
