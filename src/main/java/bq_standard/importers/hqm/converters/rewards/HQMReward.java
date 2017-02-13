package bq_standard.importers.hqm.converters.rewards;

import java.util.List;
import betterquesting.api.questing.rewards.IReward;
import com.google.gson.JsonElement;

public interface HQMReward
{
	public List<IReward> Convert(JsonElement json);
}
