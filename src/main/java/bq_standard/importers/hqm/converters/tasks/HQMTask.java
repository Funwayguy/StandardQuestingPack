package bq_standard.importers.hqm.converters.tasks;

import java.util.ArrayList;
import betterquesting.api.questing.tasks.ITask;
import com.google.gson.JsonObject;

public abstract class HQMTask
{
	public abstract ArrayList<ITask> Convert(JsonObject json);
}
