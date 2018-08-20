package bq_standard.importers.hqm.converters.rewards;

import betterquesting.api.questing.rewards.IReward;
import bq_standard.importers.hqm.HQMUtilities;
import bq_standard.rewards.RewardItem;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class HQMRewardStandard implements HQMReward
{
	@Override
	public List<IReward> Convert(JsonElement json)
	{
		List<IReward> rList = new ArrayList<IReward>();
		
		if(json == null || !json.isJsonArray())
		{
			return null;
		}
		
		RewardItem reward = new RewardItem();
		rList.add(reward);
		
		for(JsonElement je : json.getAsJsonArray())
		{
			if(je == null || !je.isJsonObject())
			{
				continue;
			}
			
			reward.items.add(HQMUtilities.HQMStackT1(je.getAsJsonObject()));
		}
		
		return rList;
	}
	
}
