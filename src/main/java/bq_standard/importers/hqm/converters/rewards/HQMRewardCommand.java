package bq_standard.importers.hqm.converters.rewards;

import betterquesting.api.questing.rewards.IReward;
import bq_standard.rewards.RewardCommand;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class HQMRewardCommand implements HQMReward
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
			if(je == null || !je.isJsonPrimitive())
			{
				continue;
			}
			
			RewardCommand reward = new RewardCommand();
			reward.command = je.getAsString();
			rList.add(reward);
		}
		
		return rList;
	}
}
