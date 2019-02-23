package bq_standard.client.gui.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import bq_standard.tasks.TaskInteractItem;

public class PanelTaskInteractItem extends CanvasEmpty
{
    private final TaskInteractItem task;
    private final IQuest quest;
    
    public PanelTaskInteractItem(IGuiRect rect, IQuest quest, TaskInteractItem task)
    {
        super(rect);
        String n = "mName";
        this.quest = quest;
        this.task = task;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        // TODO: Finish me
    }
}
