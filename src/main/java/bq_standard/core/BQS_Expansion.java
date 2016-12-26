package bq_standard.core;

import betterquesting.api.api.IQuestExpansion;
import betterquesting.api.api.QuestExpansion;

@QuestExpansion
public class BQS_Expansion implements IQuestExpansion
{
	@Override
	public void loadExpansion()
	{
		BQ_Standard.proxy.registerExpansion();
	}
}
