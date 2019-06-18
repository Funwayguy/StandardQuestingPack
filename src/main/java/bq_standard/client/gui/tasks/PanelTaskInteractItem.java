package bq_standard.client.gui.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.textures.GuiTextureColored;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import bq_standard.client.theme.BQSTextures;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskInteractItem;
import mezz.jei.Internal;
import mezz.jei.api.recipe.IFocus.Mode;
import mezz.jei.gui.Focus;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public class PanelTaskInteractItem extends CanvasEmpty
{
    private final TaskInteractItem task;
    private final IQuest quest;
    
    public PanelTaskInteractItem(IGuiRect rect, IQuest quest, TaskInteractItem task)
    {
        super(rect);
        this.quest = quest;
        this.task = task;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        PanelItemSlot itemSlot = new PanelItemSlot(new GuiTransform(GuiAlign.MID_LEFT, 0, -48, 32, 32, 0), -1, task.targetItem, false, true);
        this.addPanel(itemSlot);
        
        PanelItemSlot targetSlot = new PanelItemSlot(new GuiTransform(GuiAlign.MID_CENTER, 16, -32, 32, 32, 0), -1, task.targetBlock.getItemStack(), false, true);
        this.addPanel(targetSlot);
        
        if(BQ_Standard.hasJEI)
        {
            itemSlot.setCallback(value -> lookupRecipe(value.getBaseStack()));
            targetSlot.setCallback(value -> lookupRecipe(value.getBaseStack()));
        }
        
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_CENTER, -8, -32, 16, 16, 0), PresetIcon.ICON_RIGHT.getTexture()));
        UUID playerID = QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player);
        int prog = task.getPartyProgress(playerID);
        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.MID_CENTER, -16, -14, 32, 14, 0), prog + "/" + task.required).setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor()));
        
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_CENTER, -48, 8, 24, 24, 0), BQSTextures.HAND_LEFT.getTexture()));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_CENTER, -24, 8, 24, 24, 0), BQSTextures.HAND_RIGHT.getTexture()));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_CENTER, 0, 8, 24, 24, 0), BQSTextures.ATK_SYMB.getTexture()));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_CENTER, 24, 8, 24, 24, 0), BQSTextures.USE_SYMB.getTexture()));
        
        IGuiTexture txTick = new GuiTextureColored(PresetIcon.ICON_TICK.getTexture(), new GuiColorStatic(0xFF00FF00));
        IGuiTexture txCross = new GuiTextureColored(PresetIcon.ICON_CROSS.getTexture(), new GuiColorStatic(0xFFFF0000));
        
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_CENTER, -32, 24, 8, 8, 0), task.useOffHand ? txTick : txCross));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_CENTER, -8, 24, 8, 8, 0), task.useMainHand ? txTick : txCross));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_CENTER, 16, 24, 8, 8, 0), task.onHit ? txTick : txCross));
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_CENTER, 40, 24, 8, 8, 0), task.onInteract ? txTick : txCross));
    }
    
    private void lookupRecipe(ItemStack stack)
    {
        if(stack == null || stack.isEmpty()) return;
        Internal.getRuntime().getRecipesGui().show(new Focus<>(Mode.OUTPUT, stack));
    }
}
