package bq_standard.importers.hqm.converters.tasks;

import java.util.ArrayList;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import bq_standard.importers.hqm.HQMUtilities;
import bq_standard.tasks.TaskCrafting;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HQMTaskCraft extends HQMTask
{
	@Override
	public ArrayList<ITask> Convert(JsonObject json)
	{
		ArrayList<ITask> tList = new ArrayList<ITask>();
		
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
