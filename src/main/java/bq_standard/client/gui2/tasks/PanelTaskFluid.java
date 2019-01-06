package bq_standard.client.gui2.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelFluidSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.utils.QuestTranslation;
import bq_standard.tasks.TaskFluid;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import java.util.UUID;

public class PanelTaskFluid extends CanvasEmpty
{
    private final IQuest quest;
    private final TaskFluid task;
    
    public PanelTaskFluid(IGuiRect rect, IQuest quest, TaskFluid task)
    {
        super(rect);
        this.quest = quest;
        this.task = task;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        UUID uuid = QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player);
        int[] progress = quest == null || !quest.getProperty(NativeProps.GLOBAL) ? task.getPartyProgress(uuid) : task.getGlobalProgress();
        boolean isComplete = task.isComplete(uuid);
        
        String sCon = (task.consume? TextFormatting.RED : TextFormatting.GREEN) + QuestTranslation.translate(task.consume? "gui.yes" : "gui.no");
        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate("bq_standard.btn.consume", sCon)).setColor(PresetColor.TEXT_MAIN.getColor()));
        
        CanvasScrolling cvList = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 16, 8, 0), 0));
        this.addPanel(cvList);
    
        PanelVScrollBar scList = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 16, 0, 0), 0));
        this.addPanel(scList);
        cvList.setScrollDriverY(scList);
        
        int listW = cvList.getTransform().getWidth();
        
        for(int i = 0; i < task.requiredFluids.size(); i++)
        {
            FluidStack stack = task.requiredFluids.get(i);
            
            if(stack == null)
            {
                continue;
            }
    
            PanelFluidSlot slot = new PanelFluidSlot(new GuiRectangle(0, i * 36, 36, 36, 0), -1, stack);
            slot.setCallback(this::lookupRecipe);
            cvList.addPanel(slot);
            
            StringBuilder sb = new StringBuilder();
            
            sb.append(stack.getLocalizedName()).append("\n");
			sb.append(progress[i]).append("/").append(stack.amount).append("mB\n");
			
			if(progress[i] >= stack.amount || isComplete)
			{
				sb.append(TextFormatting.GREEN).append(QuestTranslation.translate("betterquesting.tooltip.complete"));
			} else
			{
				sb.append(TextFormatting.RED).append(QuestTranslation.translate("betterquesting.tooltip.incomplete"));
			}
            
            PanelTextBox text = new PanelTextBox(new GuiRectangle(40, i * 36, listW - 40, 36, 0), sb.toString());
			text.setColor(PresetColor.TEXT_MAIN.getColor());
			cvList.addPanel(text);
        }
    }
    
    private void lookupRecipe(FluidStack fluid)
    {
    
    }
}
