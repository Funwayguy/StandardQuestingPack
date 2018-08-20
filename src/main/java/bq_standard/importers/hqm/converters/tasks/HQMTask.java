package bq_standard.importers.hqm.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import com.google.gson.JsonObject;

import java.util.List;

public interface HQMTask
{
	public List<ITask> Convert(JsonObject json);
}
