package bq_standard.importers;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.questing.IQuestLineEntry;

public class QuestMergeUtility
{
	private final IQuestDatabase questDB;
	private final IQuestLineDatabase lineDB;
	
	public QuestMergeUtility(IQuestDatabase questDB, IQuestLineDatabase lineDB)
	{
		this.questDB = questDB;
		this.lineDB = lineDB;
	}
	
	public void merge(IQuestDatabase qdb, IQuestLineDatabase ldb)
	{
		HashMap<Integer,Integer> remapped = getRemappedIDs(qdb.getAllKeys());
		
		for(Entry<Integer,Integer> entry : remapped.entrySet())
		{
			questDB.add(qdb.getValue(entry.getKey()), entry.getValue());
		}
		
		for(IQuestLine questLine : ldb.getAllValues())
		{
			for(IQuestLineEntry qle : questLine.getAllValues())
			{
				int oldID = questLine.getKey(qle);
				questLine.removeKey(oldID);
				questLine.add(qle, remapped.get(oldID));
			}
			
			lineDB.add(questLine, lineDB.nextKey());
		}
	}
	
	private HashMap<Integer,Integer> getRemappedIDs(List<Integer> idList)
	{
		List<Integer> existing = questDB.getAllKeys();
		HashMap<Integer,Integer> remapped = new HashMap<Integer,Integer>();
		
		int n = 0;
		
		for(int id : idList)
		{
			while(existing.contains(n))
			{
				n++;
			}
			
			remapped.put(id, n);
		}
		
		return remapped;
	}
}
