package bq_standard.client.gui.tasks;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import bq_standard.tasks.TaskInteractEntity;

public class PanelTaskInteractEntity extends CanvasEmpty
{
    private final TaskInteractEntity task;
    private final IQuest quest;
    
    public PanelTaskInteractEntity(IGuiRect rect, IQuest quest, TaskInteractEntity task)
    {
        super(rect);
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
