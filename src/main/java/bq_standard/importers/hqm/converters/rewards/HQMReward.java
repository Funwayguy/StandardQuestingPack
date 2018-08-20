package bq_standard.importers.hqm.converters.rewards;

import betterquesting.api.questing.rewards.IReward;
import com.google.gson.JsonElement;

import java.util.List;

public interface HQMReward
{
	public List<IReward> Convert(JsonElement json);
}
