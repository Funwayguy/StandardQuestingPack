package bq_standard.tasks;

import java.util.UUID;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.TaskBase;
import bq_standard.client.gui.tasks.GuiTaskCheckbox;
import bq_standard.core.BQ_Standard;

public class TaskCheckbox extends TaskBase
{
	@Override
	public String getUnlocalisedName()
	{
		return BQ_Standard.MODID + ".task.checkbox";
	}

	@Override
	public void ResetProgress(UUID uuid)
	{
		super.ResetProgress(uuid);
	}

	@Override
	public void ResetAllProgress()
	{
		super.ResetAllProgress();
	}

	@Override
	public GuiEmbedded getGui(QuestInstance quest, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskCheckbox(this, screen, posX, posY, sizeX, sizeY);
	}
}
