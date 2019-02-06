package bq_standard.importers.hqm.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import bq_standard.importers.hqm.HQMUtilities;
import bq_standard.tasks.TaskCrafting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class HQMTaskCraft implements HQMTask
{
	@Override
	public List<ITask> Convert(JsonObject json)
	{
		List<ITask> tList = new ArrayList<>();
		
		TaskCrafting task = new TaskCrafting();
		
		for(JsonElement element : JsonHelper.GetArray(json, "items"))
		{
			if(element == null || !element.isJsonObject())
			{
				continue;
			}
			
			task.requiredItems.add(HQMUtilities.HQMStackT2(element.getAsJsonObject()));
		}
		
		tList.add(task);
		
		return tList;
	}
}
