package bq_standard.importers.hqm.converters.rewards;

import java.util.ArrayList;
import betterquesting.api.quests.rewards.IReward;
import com.google.gson.JsonElement;

public abstract class HQMReward
{
	public abstract ArrayList<IReward> Convert(JsonElement json);
}
