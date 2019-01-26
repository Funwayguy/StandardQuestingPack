package bq_standard.client.gui.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import bq_standard.tasks.TaskAdvancement;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextFormatting;

public class PanelTaskAdvancement extends CanvasEmpty
{
    private final TaskAdvancement task;
    private final IQuest quest;
    
    private Advancement adv;
    
    public PanelTaskAdvancement(IGuiRect rect, IQuest quest, TaskAdvancement task)
    {
        super(rect);
        this.task = task;
        this.quest = quest;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
    
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        boolean isComplete = task.isComplete(QuestingAPI.getQuestingUUID(player));
        Advancement adv = task.advID == null ? null : player.connection.getAdvancementManager().getAdvancementList().getAdvancement(task.advID);
        if(adv == null) return;
        DisplayInfo disp = adv.getDisplay();
        
        this.addPanel(new PanelGeneric(new GuiRectangle(0, 0, 24, 24, 0), PresetTexture.ITEM_FRAME.getTexture()));
        if(disp != null) this.addPanel(new PanelGeneric(new GuiRectangle(0, 0, 24, 24, -1), new ItemTexture(new BigItemStack(disp.getIcon()))));
        
        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(28, 2, 0, -12), 0), disp == null ? adv.getId().toString() : disp.getTitle().getFormattedText()).setColor(PresetColor.TEXT_MAIN.getColor()));
        String s = isComplete ? (TextFormatting.GREEN.toString() + QuestTranslation.translate("betterquesting.tooltip.complete")) : (TextFormatting.RED.toString() + QuestTranslation.translate("betterquesting.tooltip.incomplete"));
        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(28, 14, 0, -24), 0), s).setColor(PresetColor.TEXT_MAIN.getColor()));
        
        if(disp != null) this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 32, 0, 0), 0), disp.getDescription().getFormattedText()).setColor(PresetColor.TEXT_MAIN.getColor()));
    }
}
