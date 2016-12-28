package bq_standard.importers.hqm.converters.tasks;

import java.util.ArrayList;
import java.util.List;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import bq_standard.importers.hqm.HQMUtilities;
import bq_standard.tasks.TaskFluid;
import bq_standard.tasks.TaskRetrieval;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class HQMTaskDetect implements HQMTask
{
	boolean consume = false;
	
	public HQMTaskDetect(boolean consume)
	{
		this.consume = consume;
	}
	
	@Override
	public List<ITask> Convert(JsonObject json)
	{
		List<ITask> tList = new ArrayList<ITask>();
		TaskRetrieval retTask = new TaskRetrieval();
		TaskFluid fluTask = new TaskFluid();
		
		retTask.consume = this.consume;
		
		for(JsonElement je : JsonHelper.GetArray(json, "items"))
		{
			if(je == null || !je.isJsonObject())
			{
				continue;
			}
			
			JsonObject ji = je.getAsJsonObject();
			
			if(ji.has("fluid"))
			{
				fluTask.requiredFluids.add(HQMUtilities.HQMStackT3(ji));
			} else
			{
				retTask.requiredItems.add(HQMUtilities.HQMStackT2(ji));
			}
		}
		
		if(retTask.requiredItems.size() > 0)
		{
			tList.add(retTask);
		}
		
		if(fluTask.requiredFluids.size() > 0)
		{
			tList.add(fluTask);
		}
		
		return tList;
	}
}
