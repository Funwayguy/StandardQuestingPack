package bq_standard.importers.hqm.converters.tasks;

import java.util.ArrayList;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import bq_standard.tasks.TaskHunt;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HQMTaskKill extends HQMTask
{
	@Override
	public ArrayList<ITask> Convert(JsonObject json)
	{
		ArrayList<ITask> tList = new ArrayList<ITask>();
		
		for(JsonElement je : JsonHelper.GetArray(json, "mobs"))
		{
			if(je == null || !je.isJsonObject())
			{
				continue;
			}
			
			JsonObject jMob = je.getAsJsonObject();
			
			TaskHunt task = new TaskHunt();
			task.idName = JsonHelper.GetString(jMob, "mob", "Zombie");
			task.required = JsonHelper.GetNumber(jMob, "kills", 1).intValue();
			task.subtypes = !JsonHelper.GetBoolean(jMob, "exact", false);
			tList.add(task);
		}
		
		return tList;
	}
	
}
