package bq_standard.client.gui2.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.utils.QuestTranslation;
import bq_standard.tasks.TaskCrafting;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.UUID;

public class PanelTaskCrafting extends CanvasEmpty
{
    private final TaskCrafting task;
    private final IQuest quest;
    
    public PanelTaskCrafting(IGuiRect rect, IQuest quest, TaskCrafting task)
    {
        super(rect);
        this.task = task;
        this.quest = quest;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        UUID uuid = QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player);
        int[] progress = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL) ? task.getPartyProgress(uuid) : task.getGlobalProgress();
        boolean isComplete = task.isComplete(uuid);
        
        CanvasScrolling cvList = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 8, 0), 0));
        this.addPanel(cvList);
    
        PanelVScrollBar scList = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 0, 0, 0), 0));
        this.addPanel(scList);
        cvList.setScrollDriverY(scList);
        
        int listW = cvList.getTransform().getWidth();
        
        for(int i = 0; i < task.requiredItems.size(); i++)
        {
            BigItemStack stack = task.requiredItems.get(i);
            
            if(stack == null)
            {
                continue;
            }
    
            PanelItemSlot slot = new PanelItemSlot(new GuiRectangle(0, i * 36, 36, 36, 0), -1, stack, true);
            slot.setCallback(value -> lookupRecipe(value.getBaseStack()));
            cvList.addPanel(slot);
            
            StringBuilder sb = new StringBuilder();
            
            sb.append(stack.getBaseStack().getDisplayName());
			
			if(stack.oreDict.length() > 0) sb.append(" (").append(stack.oreDict).append(")");
			
			sb.append("\n").append(progress[i]).append("/").append(stack.stackSize).append("\n");
			
			if(progress[i] >= stack.stackSize || isComplete)
			{
				sb.append(TextFormatting.GREEN).append(QuestTranslation.translate("betterquesting.tooltip.complete"));
			} else
			{
				sb.append(TextFormatting.RED).append(QuestTranslation.translate("betterquesting.tooltip.incomplete"));
			}
            
            PanelTextBox text = new PanelTextBox(new GuiRectangle(40, i * 36, listW - 40, 36, 0), sb.toString());
			cvList.addPanel(text);
        }
    }
    
    private void lookupRecipe(ItemStack stack)
    {
    
    }
}
