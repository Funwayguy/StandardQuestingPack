package bq_standard.client.gui.editors.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import bq_standard.tasks.TaskAdvancement;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GuiEditTaskAdvancement extends GuiScreenCanvas implements IVolatileScreen
{
    private final IQuest quest;
    private final TaskAdvancement task;
    
    private ResourceLocation selected;
    private final List<PanelButtonStorage<Advancement>> btnList = new ArrayList<>();
    
    public GuiEditTaskAdvancement(GuiScreen parent, IQuest quest, TaskAdvancement task)
    {
        super(parent);
        this.quest = quest;
        this.task = task;
        
        selected = task.advID;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
        
        cvBackground.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(16, 16, 16, -32), 0), QuestTranslation.translate("bq_standard.title.edit_advancement")).setAlignment(1).setColor(PresetColor.TEXT_HEADER.getColor()));
    
        CanvasScrolling cvAdvList = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 32, 24, 24), 0));
        cvBackground.addPanel(cvAdvList);
    
        PanelVScrollBar scAdv = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-24, 32, 16, 24), 0));
        cvBackground.addPanel(scAdv);
        cvAdvList.setScrollDriverY(scAdv);
        
        int i = 0;
        int w = cvAdvList.getTransform().getWidth();
        
        btnList.clear();
        for(Advancement adv : mc.player.connection.getAdvancementManager().getAdvancementList().getAdvancements())
        {
            DisplayInfo disp = adv.getDisplay();
            cvAdvList.addPanel(new PanelGeneric(new GuiRectangle(0, i * 24, 24, 24, 0), PresetTexture.ITEM_FRAME.getTexture()));
            if(disp != null)cvAdvList.addPanel(new PanelGeneric(new GuiRectangle(0, i * 24, 24, 24, -1), new ItemTexture(new BigItemStack(disp.getIcon()))));
            
            PanelButtonStorage<Advancement> btnAdv = new PanelButtonStorage<>(new GuiRectangle(24, i * 24, w - 24, 24, 0), -1, disp != null ? disp.getTitle().getFormattedText() : adv.getId().toString(), adv);
            btnAdv.setActive(!adv.getId().equals(selected));
            btnAdv.setCallback(value -> {
                selected = value.getId();
                for(PanelButtonStorage<Advancement> b : btnList) b.setActive(!b.getStoredValue().getId().equals(selected));
            });
            if(disp != null)
            {
                btnAdv.setTooltip(RenderUtils.splitString(disp.getDescription().getFormattedText(), 128, mc.fontRenderer));
            }
            cvAdvList.addPanel(btnAdv);
            btnList.add(btnAdv);
            i++;
        }
        
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), -1, QuestTranslation.translate("gui.done"))
        {
            @Override
            public void onButtonClick()
            {
                sendChanges();
                mc.displayGuiScreen(parent);
            }
        });
    }
    
    private static final ResourceLocation QUEST_EDIT = new ResourceLocation("betterquesting:quest_edit"); // TODO: Really need to make the native packet types accessible in the API
    private void sendChanges()
    {
        task.advID = selected;
		NBTTagCompound base = new NBTTagCompound();
		base.setTag("config", quest.writeToNBT(new NBTTagCompound()));
		base.setTag("progress", quest.writeProgressToNBT(new NBTTagCompound(), null)); // TODO: Remove this when partial writes are implemented
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal()); // Action: Update data
		tags.setInteger("questID", QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
		tags.setTag("data",base);
		QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(QUEST_EDIT, tags));
    }
}
