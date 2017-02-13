package bq_standard.importers.hqm.converters.tasks;

import java.util.List;
import betterquesting.api.questing.tasks.ITask;
import com.google.gson.JsonObject;

public interface HQMTask
{
	public List<ITask> Convert(JsonObject json);
}
