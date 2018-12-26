package bq_standard.client.gui2.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import bq_standard.network.StandardPacketType;
import bq_standard.tasks.TaskCheckbox;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;

public class PanelTaskCheckbox extends CanvasEmpty
{
    private final IQuest quest;
    private final TaskCheckbox task;
    
    public PanelTaskCheckbox(IGuiRect rect, IQuest quest, TaskCheckbox task)
    {
        super(rect);
        this.quest = quest;
        this.task = task;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        boolean isComplete = task.isComplete(QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player));
    
        PanelButton btnCheck = new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, -16, -16, 32, 32, 0), -1, "")
        {
            @Override
            public void onButtonClick()
            {
                setIcon(PresetIcon.ICON_TICK.getTexture(), new GuiColorStatic(0xFF00FF00), 4);
                setActive(false);
                
                NBTTagCompound tags = new NBTTagCompound();
                tags.setInteger("ID", 2);
                tags.setInteger("qId", QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
                tags.setInteger("tId", quest.getTasks().getID(task));
                QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(StandardPacketType.CHECKBOX.GetLocation(), tags));
            }
        };
        btnCheck.setIcon(isComplete ? PresetIcon.ICON_TICK.getTexture() : PresetIcon.ICON_CROSS.getTexture(), new GuiColorStatic(isComplete ? 0xFF00FF00 : 0xFFFF0000), 4);
        btnCheck.setActive(!isComplete);
        this.addPanel(btnCheck);
    }
}
