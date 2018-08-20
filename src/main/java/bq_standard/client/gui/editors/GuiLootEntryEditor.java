package bq_standard.client.gui.editors;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.controls.GuiNumberField;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.RenderUtils;
import bq_standard.rewards.loot.LootGroup;
import bq_standard.rewards.loot.LootGroup.LootEntry;
import bq_standard.rewards.loot.LootRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiLootEntryEditor extends GuiScreenThemed implements IVolatileScreen, ICallback<NBTTagCompound>
{
	LootGroup group;
	GuiNumberField lineWeight;
	LootEntry selected;
	int selIndex = -1;
	int leftScroll = 0;
	int maxRows = 0;
	//JsonObject lastEdit;
	
	public GuiLootEntryEditor(GuiScreen parent, LootGroup group)
	{
		super(parent, "bq_standard.title.edit_loot_entries");
		this.group = group;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		/*if(lastEdit != null && selected != null)
		{
			selected.readFromJson(lastEdit);
		}
		
		lastEdit = null;*/
		//selected = null;
		
		maxRows = (sizeY - 80)/20;
		int btnWidth = sizeX/2 - 16;
		int sx = sizeX - 32;
		
		lineWeight = new GuiNumberField(mc.fontRenderer, guiLeft + sizeX/2 + 9, guiTop + sizeY/2 - 19, btnWidth/2 - 10, 18);
		lineWeight.setMaxStringLength(Integer.MAX_VALUE);
		 
		this.buttonList.add(new GuiButtonThemed(1, guiLeft + 16 + sx/4 - 50, guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.new")));
		this.buttonList.add(new GuiButtonThemed(2, guiLeft + 16 + sx/4*3 - 75, guiTop + sizeY/2 + 20, 150, 20, I18n.format("bq_standard.btn.add_remove_drops")));
		
		// Quest Line - Main
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + 36, guiTop + 32 + (i*20), btnWidth - 36, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		// Quest Line - Delete
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + 16, guiTop + 32 + (i*20), 20, 20, "" + TextFormatting.RED + TextFormatting.BOLD + "x");
			this.buttonList.add(btn);
		}
		
		RefreshColumns();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(LootRegistry.updateUI)
		{
			RefreshColumns();
			LootRegistry.updateUI = false;
		}
		
		GlStateManager.color(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(this.currentTheme().getGuiTexture());
		
		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 32 + (int)Math.max(0, s * (float)leftScroll/(group.lootEntry.size() - maxRows)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 48, 2F, this.getTextColor());
		
		mc.fontRenderer.drawString(I18n.format("betterquesting.gui.name"), guiLeft + sizeX/2 + 8, guiTop + sizeY/2 - 72, this.getTextColor(), false);
		mc.fontRenderer.drawString(TextFormatting.BOLD + group.name, guiLeft + sizeX/2 + 8, guiTop + sizeY/2 - 52, this.getTextColor(), false);
		mc.fontRenderer.drawString(I18n.format("bq_standard.gui.weight"), guiLeft + sizeX/2 + 8, guiTop + sizeY/2 - 32, this.getTextColor(), false);
		
		if(selected != null)
		{
			mc.fontRenderer.drawString("" + new DecimalFormat("#.##").format((float)selected.weight/(float)group.getTotalWeight() * 100F) + "%", guiLeft + 16 + (sizeX - 32)/4*3 + 8, guiTop + sizeY/2 - 14, this.getTextColor(), false);
		}
		
		lineWeight.drawTextBox();
	}
	
	@Override
	public void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
		
		if(btn.id == 1) // Add quest line
		{
			group.lootEntry.add(new LootEntry());
			RefreshColumns();
		} else if(btn.id == 2) // Add loot group
		{
			if(selected != null)
			{
				NBTTagCompound json = new NBTTagCompound();
				selected.writeToJson(json);
				QuestingAPI.getAPI(ApiReference.GUI_HELPER).openJsonEditor(this, this, json, null);
			}
			
		} else if(btn.id > 2)
		{
			int n1 = btn.id - 3; // Line index
			int n2 = n1/maxRows; // Line listing (0 = line, 1 = delete)
			int n3 = n1%maxRows + leftScroll; // Quest list index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < group.lootEntry.size())
				{
					selected = group.lootEntry.get(n3);
					selIndex = n3;
				} else
				{
					selected = null;
					selIndex = -1;
				}
				
				RefreshColumns();
			} else if(n2 == 1)
			{
				if(n3 >= 0 && n3 < group.lootEntry.size())
				{
					group.lootEntry.remove(n3);
				}
			}
		}
	}

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int keyCode) throws IOException
    {
        super.keyTyped(character, keyCode);
        
        if(selected != null)
        {
        	lineWeight.textboxKeyTyped(character, keyCode);
        }
    }
	
    /**
     * Called when the mouse is clicked.
     */
	@Override
    protected void mouseClicked(int mx, int my, int click) throws IOException
    {
		super.mouseClicked(mx, my, click);
		
		lineWeight.mouseClicked(mx, my, click);
		
		if(selected != null)
		{
			if(!lineWeight.isFocused() && lineWeight.getNumber().intValue() != selected.weight)
			{
				selected.weight = lineWeight.getNumber().intValue();
			}
		}
    }
	
    /**
     * Handles mouse input.
     */
	@Override
    public void handleMouseInput() throws IOException
    {
		super.handleMouseInput();
		
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, sizeX/2, sizeY))
        {
    		leftScroll = Math.max(0, MathHelper.clamp(leftScroll + SDX, 0, group.lootEntry.size() - maxRows));
    		RefreshColumns();
        }
    }
	
	public void RefreshColumns()
	{
		leftScroll = Math.max(0, MathHelper.clamp(leftScroll, 0, group.lootEntry.size() - maxRows));
		
		if(selected != null && !group.lootEntry.contains(selected))
		{
			if(selIndex >= 0 && selIndex < group.lootEntry.size())
			{
				selected = group.lootEntry.get(selIndex);
			} else
			{
				selected = null;
				selIndex = -1;
			}
		}

		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 3; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = btn.id - 3; // Line index
			int n2 = n1/maxRows; // Line listing (0 = line, 1 = delete)
			int n3 = n1%maxRows + leftScroll; // Quest list index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < group.lootEntry.size())
				{
					btn.displayString = "#" + n3;
					btn.enabled = btn.visible = true;
				} else
				{
					btn.displayString = "NULL";
					btn.enabled = btn.visible = false;
				}
			} else if(n2 == 1)
			{
				btn.enabled = btn.visible = n3 >= 0 && n3 < group.lootEntry.size();
			}
		}
		
		if(selected == null)
		{
			lineWeight.setText("");
			lineWeight.setEnabled(false);
		} else
		{
			lineWeight.setText("" + selected.weight);
			lineWeight.setEnabled(true);
		}
	}

	@Override
	public void setValue(NBTTagCompound value)
	{
		if(selected != null)
		{
			selected.readFromJson(value);
		}
	}
}
