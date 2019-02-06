package bq_standard.client.gui.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.utils.QuestTranslation;
import bq_standard.tasks.TaskLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import org.lwjgl.util.vector.Vector4f;

import java.awt.*;

public class PanelTaskLocation extends CanvasEmpty
{
    private final IQuest quest;
    private final TaskLocation task;
    
    public PanelTaskLocation(IGuiRect rect, IQuest quest, TaskLocation task)
    {
        super(rect);
        this.quest = quest;
        this.task = task;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        String desc = QuestTranslation.translate(task.name);
        
        if(!task.hideInfo)
        {
            desc += " (" + getDimName(task.dim) + ")";
            
            if(task.range >= 0)
            {
                desc += "\n" + QuestTranslation.translate("bq_standard.gui.location", "(" + task.x + ", " + task.y + ", " + task.z + ")");
                desc += "\n" + QuestTranslation.translate("bq_standard.gui.distance", (int)Minecraft.getMinecraft().player.getDistance(task.x, task.y, task.z) + "m");
            }
        }
        
        if(task.isComplete(QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player)))
        {
            desc += "\n" + TextFormatting.BOLD + TextFormatting.GREEN + QuestTranslation.translate("bq_standard.gui.found");
        } else
        {
            desc += "\n" + TextFormatting.BOLD + TextFormatting.RED + QuestTranslation.translate("bq_standard.gui.undiscovered");
        }
        
        this.addPanel(new PanelTextBox(new GuiTransform(new Vector4f(0F, 0F, 1F, 0.3F), new GuiPadding(0, 0, 0, 0), 0), desc).setColor(PresetColor.TEXT_MAIN.getColor()));
        
        IGuiTexture texCompass = new IGuiTexture()
        {
            @Override
            public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick)
            {
                drawTexture(x, y, width, height, zDepth, partialTick, null);
            }
    
            @Override
            public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick, IGuiColor color)
            {
                Minecraft mc = Minecraft.getMinecraft();
                
		        double la = Math.atan2(task.z - mc.player.posZ, task.x - mc.player.posX);
                int radius = width/2 - 12;
                int cx = x + width/2;
                int cy = y + height/2;
                int dx = (int)(Math.cos(la) * radius);
                int dy = (int)(Math.sin(la) * -radius);
                int txtClr = color == null ? 0xFFFFFFFF : color.getRGB();
                
                Gui.drawRect(cx - radius, cy - radius, cx + radius, cy + radius, Color.BLACK.getRGB());
                RenderUtils.DrawLine(cx - radius, cy - radius, cx + radius, cy - radius, 4, Color.WHITE.getRGB());
                RenderUtils.DrawLine(cx - radius, cy - radius, cx - radius, cy + radius, 4, Color.WHITE.getRGB());
                RenderUtils.DrawLine(cx + radius, cy + radius, cx + radius, cy - radius, 4, Color.WHITE.getRGB());
                RenderUtils.DrawLine(cx + radius, cy + radius, cx - radius, cy + radius, 4, Color.WHITE.getRGB());
                mc.fontRenderer.drawString(TextFormatting.BOLD + "N", cx - 4, cy - radius - 9, txtClr);
                mc.fontRenderer.drawString(TextFormatting.BOLD + "S", cx - 4, cy + radius + 2, txtClr);
                mc.fontRenderer.drawString(TextFormatting.BOLD + "E", cx + radius + 2, cy - 4, txtClr);
                mc.fontRenderer.drawString(TextFormatting.BOLD + "W", cx - radius - 8, cy - 4, txtClr);
                
                if(task.hideInfo || task.range < 0 || mc.player.dimension != task.dim)
                {
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(2F, 2F, 2F);
                    mc.fontRenderer.drawString(TextFormatting.BOLD + "?", cx/2 - 4, cy/2 - 4 , Color.RED.getRGB());
                    GlStateManager.popMatrix();
                } else
                {
                    RenderUtils.DrawLine(cx, cy, cx + dx, cy - dy, 4, Color.RED.getRGB());
                }
            }
    
            @Override
            public ResourceLocation getTexture()
            {
                return null;
            }
    
            @Override
            public IGuiRect getBounds()
            {
                return null;
            }
        };
        
        int innerSize = Math.min(this.getTransform().getWidth(), (int)Math.floor(this.getTransform().getHeight() * 0.7D));
        PanelGeneric innerCanvas = new PanelGeneric(new GuiTransform(new Vector4f(0.5F, 0.65F, 0.5F, 0.65F), -innerSize/2, -innerSize/2, innerSize, innerSize, 0), texCompass, PresetColor.TEXT_MAIN.getColor());
        this.addPanel(innerCanvas);
    }
    
    private static String getDimName(int dim)
	{
	    try
        {
            return DimensionType.getById(dim).getName();
        } catch(Exception e)
        {
            return "?";
        }
	}
}
