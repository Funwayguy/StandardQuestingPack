package bq_standard.importers.hqm;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.init.Items;
import org.apache.logging.log4j.Level;
import betterquesting.api.client.importers.IImporter;
import betterquesting.api.properties.IPropertyContainer;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.storage.IRegStorageBase;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.FileExtensionFilter;
import betterquesting.api.utils.JsonHelper;
import bq_standard.core.BQ_Standard;
import bq_standard.importers.hqm.converters.rewards.HQMReward;
import bq_standard.importers.hqm.converters.rewards.HQMRewardChoice;
import bq_standard.importers.hqm.converters.rewards.HQMRewardCommand;
import bq_standard.importers.hqm.converters.rewards.HQMRewardReputation;
import bq_standard.importers.hqm.converters.rewards.HQMRewardStandard;
import bq_standard.importers.hqm.converters.tasks.HQMTask;
import bq_standard.importers.hqm.converters.tasks.HQMTaskCraft;
import bq_standard.importers.hqm.converters.tasks.HQMTaskDetect;
import bq_standard.importers.hqm.converters.tasks.HQMTaskKill;
import bq_standard.importers.hqm.converters.tasks.HQMTaskLocation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HQMQuestImporter implements IImporter
{
	public static HQMQuestImporter instance = new HQMQuestImporter();
	
	public static HashMap<String, HQMTask> taskConverters = new HashMap<String, HQMTask>();
	public static HashMap<String, HQMReward> rewardConverters = new HashMap<String, HQMReward>();
	
	public HashMap<Integer, String> reputations = new HashMap<Integer, String>();
	
	public HashMap<String, IQuest> idMap = new HashMap<String, IQuest>(); // Use this to remap old IDs to new ones
	
	@Override
	public FileFilter getFileFilter()
	{
		return new FileExtensionFilter(".json");
	}

	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.importer.hqm_quest.name";
	}

	@Override
	public String getUnlocalisedDescription()
	{
		return "bq_standard.importer.hqm_quest.desc";
	}

	@Override
	public void loadFiles(IQuestDatabase questDB, IQuestLineDatabase lineDB, File[] files)
	{
		reputations.clear();
		idMap.clear();
		
		for(File selected : files)
		{
			if(selected == null || !selected.exists())
			{
				continue;
			}
			
			JsonObject json = JsonHelper.ReadFromFile(selected);
			ImportQuestLine(questDB, lineDB, json);
		}
	}
	
	private void LoadReputations(JsonObject jsonRoot)
	{
		reputations.clear();
		
		int i = -1;
		
		for(JsonElement e : JsonHelper.GetArray(jsonRoot, "reputation"))
		{
			i++;
			
			if(e == null || !e.isJsonObject())
			{
				continue;
			}
			
			JsonObject jRep = e.getAsJsonObject();
			
			if(!jRep.has("name"))
			{
				continue;
			} else
			{
				reputations.put(i, JsonHelper.GetString(jRep, "name", "Reputation(" + i + ")"));
			}
		}
	}
	
	private IQuest GetNewQuest(String oldID, IQuestDatabase qdb)
	{
		if(idMap.containsKey(oldID))
		{
			return idMap.get(oldID);
		} else
		{
			IQuest quest = qdb.createNew();
			qdb.add(quest, qdb.nextKey());
			idMap.put(oldID, quest);
			return quest;
		}
	}
	
	private void ImportQuestLine(IQuestDatabase questDB, IQuestLineDatabase lineDB, JsonObject json)
	{
		IQuestLine questLine = lineDB.createNew();
		IPropertyContainer qlProps = questLine.getProperties();
		qlProps.setProperty(NativeProps.NAME, JsonHelper.GetString(json, "name", "HQM Quest Line"));
		qlProps.setProperty(NativeProps.DESC, JsonHelper.GetString(json, "description", "No description"));
		
		LoadReputations(json);
		
		JsonArray qlJson = JsonHelper.GetArray(json, "quests");
		
		ArrayList<String> loadedQuests = new ArrayList<String>(); // Just in case we have duplicate named quests
		
		for(int i = 0; i < qlJson.size(); i++)
		{
			JsonElement element = qlJson.get(i);
			
			if(element == null || !element.isJsonObject())
			{
				continue;
			}
			
			JsonObject jQuest = element.getAsJsonObject();
			
			String name = JsonHelper.GetString(jQuest, "name", "HQM Quest");
			String idName = jQuest.has("uuid")? JsonHelper.GetString(jQuest, "uuid", name) : name;
			
			if(loadedQuests.contains(idName))
			{
				int n = 1;
				while(loadedQuests.contains(idName + " (" + n + ")"))
				{
					n++;
				}
				BQ_Standard.logger.log(Level.WARN, "Found duplicate quest " + name + ". Any quests with this pre-requisite will need repair!");
				idName = name + " (" + n + ")";
			}
			
			loadedQuests.add(idName);
			IQuest quest = GetNewQuest(idName, questDB);
			
			IPropertyContainer qProps = quest.getProperties();
			qProps.setProperty(NativeProps.NAME, name);
			qProps.setProperty(NativeProps.DESC, JsonHelper.GetString(jQuest, "description", "No Description"));
			BigItemStack tmp = HQMUtilities.HQMStackT1(JsonHelper.GetObject(jQuest, "icon"));
			
			if(tmp != null)
			{
				qProps.setProperty(NativeProps.ICON, tmp);
			} else
			{
				qProps.setProperty(NativeProps.ICON, new BigItemStack(Items.nether_star));
			}
			
			if(json.has("repeat")) // Assuming this is in Minecraft time
			{
				JsonObject jRpt = JsonHelper.GetObject(jQuest, "repeat");
				int rTime = 0;
				rTime += JsonHelper.GetNumber(jRpt, "days", 0).intValue() * 24000;
				rTime += JsonHelper.GetNumber(jRpt, "hours", 0).intValue() * 1000;
				qProps.setProperty(NativeProps.REPEAT_TIME, rTime);
			}
			
			for(JsonElement er : JsonHelper.GetArray(jQuest, "prerequisites"))
			{
				if(er == null || !er.isJsonPrimitive() || !er.getAsJsonPrimitive().isString())
				{
					continue;
				}
				
				String id = er.getAsJsonPrimitive().getAsString();
				
				if(id.startsWith("{") && id.contains("["))
				{
					String[] nParts = id.split("\\[");
					
					if(nParts.length > 1)
					{
						id = nParts[1].replaceFirst("]", "");
					}
				}
				
				IQuest preReq = GetNewQuest(id, questDB);
				quest.getPrerequisites().add(preReq);
			}
			
			for(JsonElement jt : JsonHelper.GetArray(jQuest, "tasks"))
			{
				if(jt == null || !jt.isJsonObject())
				{
					continue;
				}
				
				JsonObject jTask = jt.getAsJsonObject();
				String tType = JsonHelper.GetString(jTask, "type", "");
				
				if(tType == null || tType.length() <= 0)
				{
					continue;
				} else if(!taskConverters.containsKey(tType))
				{
					BQ_Standard.logger.log(Level.WARN, "Unidentified HQM task '" + tType + "'! Please report this so that it can be supported in future builds");
					continue;
				}
				
				List<ITask> tsks = taskConverters.get(tType).Convert(jTask);
				
				if(tsks != null && tsks.size() > 0)
				{
					IRegStorageBase<Integer,ITask> taskReg = quest.getTasks();
					for(ITask t : tsks)
					{
						taskReg.add(t, taskReg.nextKey());
					}
				}
			}
			
			for(Entry<String,HQMReward> entry : rewardConverters.entrySet())
			{
				if(!jQuest.has(entry.getKey()))
				{
					continue;
				}
				
				List<IReward> rews = entry.getValue().Convert(jQuest.get(entry.getKey()));
				
				if(rews != null && rews.size() > 0)
				{
					IRegStorageBase<Integer,IReward> rewardReg = quest.getRewards();
					for(IReward r : rews)
					{
						rewardReg.add(r, rewardReg.nextKey());
					}
				}
			}
			
			if(questLine.getValue(questDB.getKey(quest)) != null)
			{
				BQ_Standard.logger.log(Level.WARN, "Tried to add duplicate quest " + quest + " to quest line " + questLine.getUnlocalisedName());
			} else
			{
				IQuestLineEntry qle = questLine.createNewEntry();
				qle.setPosition(JsonHelper.GetNumber(jQuest, "x", 0).intValue(), JsonHelper.GetNumber(jQuest, "y", 0).intValue());
				questLine.add(qle, questDB.getKey(quest));
			}
		}
		
		lineDB.add(questLine, lineDB.nextKey());
	}
	
	static
	{
		taskConverters.put("DETECT", new HQMTaskDetect(false));
		taskConverters.put("CONSUME", new HQMTaskDetect(true));
		taskConverters.put("CONSUME_QDS", new HQMTaskDetect(true));
		taskConverters.put("KILL", new HQMTaskKill());
		taskConverters.put("LOCATION", new HQMTaskLocation());
		taskConverters.put("CRAFT", new HQMTaskCraft());
		
		rewardConverters.put("reward", new HQMRewardStandard());
		rewardConverters.put("rewardchoice", new HQMRewardChoice());
		rewardConverters.put("reputationrewards", new HQMRewardReputation());
		rewardConverters.put("commandrewards", new HQMRewardCommand());
	}
}
