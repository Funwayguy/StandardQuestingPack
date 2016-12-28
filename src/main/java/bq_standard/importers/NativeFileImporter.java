package bq_standard.importers;

import java.io.File;
import java.io.FileFilter;
import betterquesting.api.client.importers.IImporter;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.utils.FileExtensionFilter;
import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonObject;

public class NativeFileImporter implements IImporter
{
	public static NativeFileImporter instance = new NativeFileImporter();
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.importer.nat_file.name";
	}
	
	@Override
	public String getUnlocalisedDescription()
	{
		return "bq_standard.importer.nat_file.desc";
	}
	
	@Override
	public FileFilter getFileFilter()
	{
		return new FileExtensionFilter(".json");
	}

	@Override
	public void loadFiles(IQuestDatabase questDB, IQuestLineDatabase lineDB, File[] files)
	{
		QuestMergeUtility mergeUtil = new QuestMergeUtility(questDB, lineDB);
		
		for(File selected : files)
		{
			if(selected == null || !selected.exists())
			{
				continue;
			}
			
			JsonObject json = JsonHelper.ReadFromFile(selected);
			
			ImportedQuests impQ = new ImportedQuests(questDB);
			ImportedQuestLines impL = new ImportedQuestLines(lineDB); // If I can stop this from exceeding 27 (making dummy parents) we might be able to fix the base in the same way
			
			impQ.readFromJson(JsonHelper.GetArray(json, "questDatabase"), EnumSaveType.CONFIG);
			impL.readFromJson(JsonHelper.GetArray(json, "questLines"), EnumSaveType.CONFIG);
			
			mergeUtil.merge(impQ, impL);
		}
	}
}
