package bq_standard.importers.hqm.converters.rewards;

import java.util.ArrayList;
import java.util.List;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.utils.JsonHelper;
import bq_standard.importers.hqm.HQMQuestImporter;
import bq_standard.rewards.RewardScoreboard;
import com.google.gson.JsonElement;

public class HQMRewardReputation implements HQMReward
{
	@Override
	public List<IReward> Convert(JsonElement json)
	{
		List<IReward> rList = new ArrayList<IReward>();
		
		if(json == null || !json.isJsonArray())
		{
			return rList;
		}
		
		for(JsonElement je : json.getAsJsonArray())
		{
			if(je == null || !je.isJsonObject())
			{
				continue;
			}
			
			int index = JsonHelper.GetNumber(je.getAsJsonObject(), "reputation", 0).intValue();
			int value = JsonHelper.GetNumber(je.getAsJsonObject(), "value", 1).intValue();
			String name = HQMQuestImporter.instance.reputations.containsKey(index)? HQMQuestImporter.instance.reputations.get(index) : "Reputation (" + index + ")";
			RewardScoreboard reward = new RewardScoreboard();
			reward.score = name;
			reward.value = value;
			rList.add(reward);
		}
		
		return rList;
	}
}
