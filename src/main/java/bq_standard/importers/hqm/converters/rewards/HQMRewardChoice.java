package bq_standard.importers.hqm.converters.rewards;

import java.util.ArrayList;
import betterquesting.api.questing.rewards.IReward;
import bq_standard.importers.hqm.HQMUtilities;
import bq_standard.rewards.RewardChoice;
import com.google.gson.JsonElement;

public class HQMRewardChoice extends HQMReward
{
	@Override
	public ArrayList<IReward> Convert(JsonElement json)
	{
		ArrayList<IReward> rList = new ArrayList<IReward>();
		
		if(json == null || !json.isJsonArray())
		{
			return rList;
		}
		
		RewardChoice reward = new RewardChoice();
		rList.add(reward);
		
		for(JsonElement je : json.getAsJsonArray())
		{
			if(je == null || !je.isJsonObject())
			{
				continue;
			}
			
			reward.choices.add(HQMUtilities.HQMStackT1(je.getAsJsonObject()));
		}
		
		return rList;
	}
	
}
