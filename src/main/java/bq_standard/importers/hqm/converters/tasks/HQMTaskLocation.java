package bq_standard.importers.hqm.converters.tasks;

import java.util.ArrayList;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import bq_standard.tasks.TaskLocation;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HQMTaskLocation extends HQMTask
{
	@Override
	public ArrayList<ITask> Convert(JsonObject json)
	{
		ArrayList<ITask> tList = new ArrayList<ITask>();
		
		for(JsonElement element : JsonHelper.GetArray(json, "locations"))
		{
			if(element == null || !element.isJsonObject())
			{
				continue;
			}
			
			JsonObject jLoc = element.getAsJsonObject();
			
			TaskLocation task = new TaskLocation();
			task.name = JsonHelper.GetString(jLoc, "name", "New Location");
			task.x = JsonHelper.GetNumber(jLoc, "posX", 0).intValue();
			task.y = JsonHelper.GetNumber(jLoc, "posY", 0).intValue();
			task.z = JsonHelper.GetNumber(jLoc, "posZ", 0).intValue();
			task.dim = JsonHelper.GetNumber(jLoc, "dim", 0).intValue();
			task.range = JsonHelper.GetNumber(jLoc, "radius", -1).intValue();
			task.hideInfo = JsonHelper.GetString(jLoc, "", "").equalsIgnoreCase("NONE");
			tList.add(task);
		}
		
		return tList;
	}
}
