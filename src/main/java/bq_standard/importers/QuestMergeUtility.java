package bq_standard.importers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.storage.DBEntry;

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
		HashMap<Integer,Integer> remapped = getRemappedIDs(stripKeys(qdb.getEntries()));
		
		for(Entry<Integer,Integer> entry : remapped.entrySet())
		{
			questDB.add(entry.getValue(), qdb.getValue(entry.getKey()));
		}
		
		for(DBEntry<IQuestLine> questLine : ldb.getEntries())
		{
			for(DBEntry<IQuestLineEntry> qle : questLine.getValue().getEntries())
			{
				int oldID = qle.getID();
				questLine.getValue().removeID(oldID);
				questLine.getValue().add(remapped.get(oldID), qle.getValue());
			}
			
			lineDB.add(lineDB.nextID(), questLine.getValue());
		}
	}
	
	private HashMap<Integer,Integer> getRemappedIDs(List<Integer> idList)
	{
		List<Integer> existing = stripKeys(questDB.getEntries());
		HashMap<Integer,Integer> remapped = new HashMap<>();
		
		int n = 0;
		
		for(int id : idList)
		{
			while(existing.contains(n) || remapped.containsValue(n))
			{
				n++;
			}
			
			remapped.put(id, n);
		}
		
		return remapped;
	}
	
	private List<Integer> stripKeys(DBEntry[] entries)
	{
		List<Integer> keyList = new ArrayList<>();
		
		for(DBEntry e : entries)
		{
			keyList.add(e.getID());
		}
		
		return keyList;
	}
}
