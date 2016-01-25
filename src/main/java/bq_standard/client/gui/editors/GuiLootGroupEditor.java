package bq_standard.client.gui.editors;

import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiNumberField;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import bq_standard.core.BQ_Standard;
import bq_standard.network.PacketStandard;
import bq_standard.rewards.loot.LootGroup;
import bq_standard.rewards.loot.LootRegistry;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLootGroupEditor extends GuiQuesting
{
	GuiTextField lineTitle;
	GuiNumberField lineWeight;
	LootGroup selected;
	int selIndex = -1;
	int leftScroll = 0;
	int maxRows = 0;
	
	public GuiLootGroupEditor(GuiScreen parent)
	{
		super(parent, "bq_standard.title.edit_loot_groups");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		selected = null;
		
		maxRows = (sizeY - 80)/20;
		int btnWidth = Math.min(sizeX/2 - 24, 198);
		
		lineTitle = new GuiTextField(mc.fontRenderer, guiLeft + sizeX/4*3 - (btnWidth/2 + 4) + 1, height/2 - 59, btnWidth + 8 - 2, 18);
		lineTitle.setMaxStringLength(Integer.MAX_VALUE);
		
		lineWeight = new GuiNumberField(mc.fontRenderer, guiLeft + sizeX/4*3 - (btnWidth/2 + 4) + 1, height/2 - 19, btnWidth + 8 - 2, 18);
		lineWeight.setMaxStringLength(Integer.MAX_VALUE);
		 
		this.buttonList.add(new GuiButtonQuesting(1, guiLeft + sizeX/4 - (btnWidth/2 + 4), guiTop + sizeY - 48, btnWidth, 20, I18n.format("betterquesting.btn.new")));
		this.buttonList.add(new GuiButtonQuesting(2, guiLeft + sizeX/4*3 - 75, height/2 + 20, 150, 20, I18n.format("bq_standard.btn.add_remove_drops")));
		
		// Quest Line - Main
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4 - (btnWidth/2 + 4) + 20, guiTop + 32 + (i*20), btnWidth - 20, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		// Quest Line - Delete
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/4 - (btnWidth/2 + 4), guiTop + 32 + (i*20), 20, 20, "" + ChatFormatting.RED + ChatFormatting.BOLD + "x");
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
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		
		int btnWidth = Math.min(sizeX/2 - 24, 198);
		
		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 32, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/4 - 4 + btnWidth/2, this.guiTop + 32 + (int)Math.max(0, s * (float)leftScroll/(LootRegistry.lootGroups.size() - maxRows)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 48, 2F, ThemeRegistry.curTheme().textColor());
		
		mc.fontRenderer.drawString(I18n.format("betterquesting.gui.name"), guiLeft + sizeX/4*3 - (btnWidth/2 + 4), height/2 - 72, ThemeRegistry.curTheme().textColor().getRGB(), false);
		mc.fontRenderer.drawString(I18n.format("bq_standard.gui.weight"), guiLeft + sizeX/4*3 - (btnWidth/2 + 4), height/2 - 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		lineTitle.drawTextBox();
		lineWeight.drawTextBox();
	}
	
	public void SendChanges()
	{
		JsonObject json = new JsonObject();
		LootRegistry.writeToJson(json);
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", 1);
		tags.setTag("Database", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		BQ_Standard.instance.network.sendToServer(new PacketStandard(tags));
	}
	
	@Override
	public void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
		
		if(btn.id == 1) // Add quest line
		{
			LootRegistry.lootGroups.add(new LootGroup());
			SendChanges();
			RefreshColumns();
		} else if(btn.id == 2) // Add loot group
		{
			if(selected != null)
			{
				mc.displayGuiScreen(new GuiLootEntryEditor(this, selected));
			}
		} else if(btn.id > 2)
		{
			int n1 = btn.id - 3; // Line index
			int n2 = n1/maxRows; // Line listing (0 = line, 1 = delete)
			int n3 = n1%maxRows + leftScroll; // Quest list index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < LootRegistry.lootGroups.size())
				{
					selected = LootRegistry.lootGroups.get(n3);
					selIndex = n3;
				} else
				{
					selected = null;
					selIndex = -1;
				}
				
				RefreshColumns();
			} else if(n2 == 1)
			{
				if(n3 >= 0 && n3 < LootRegistry.lootGroups.size())
				{
					LootRegistry.lootGroups.remove(n3);
					SendChanges();
				}
			}
		}
	}

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int keyCode)
    {
        super.keyTyped(character, keyCode);
        
        if(selected != null)
        {
        	lineWeight.textboxKeyTyped(character, keyCode);
        	lineTitle.textboxKeyTyped(character, keyCode);
        }
    }
	
    /**
     * Called when the mouse is clicked.
     */
	@Override
    protected void mouseClicked(int mx, int my, int click)
    {
		super.mouseClicked(mx, my, click);
		
		lineTitle.mouseClicked(mx, my, click);
		lineWeight.mouseClicked(mx, my, click);
		
		if(selected != null)
		{
			boolean flag = false;
			
			if(!lineTitle.isFocused() && !lineTitle.getText().equals(selected.name))
			{
				selected.name = lineTitle.getText();
				flag = true;
			}
			
			if(!lineWeight.isFocused() && lineWeight.getNumber().intValue() != selected.weight)
			{
				selected.weight = lineWeight.getNumber().intValue();
				flag = true;
			}
			
			if(flag)
			{
				SendChanges();
			}
		}
    }
	
    /**
     * Handles mouse input.
     */
	@Override
    public void handleMouseInput()
    {
		super.handleMouseInput();
		
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, sizeX/2, sizeY))
        {
    		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll + SDX, 0, LootRegistry.lootGroups.size() - maxRows));
    		RefreshColumns();
        }
    }
	
	public void RefreshColumns()
	{
		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll, 0, LootRegistry.lootGroups.size() - maxRows));
		
		if(selected != null && !LootRegistry.lootGroups.contains(selected))
		{
			if(selIndex >= 0 && selIndex < LootRegistry.lootGroups.size())
			{
				selected = LootRegistry.lootGroups.get(selIndex);
			} else
			{
				selected = null;
				selIndex = -1;
			}
		}

		@SuppressWarnings("unchecked")
		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 3; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = btn.id - 3; // Line index
			int n2 = n1/maxRows; // Line listing (0 = line, 1 = delete)
			int n3 = n1%maxRows + leftScroll; // Quest list index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < LootRegistry.lootGroups.size())
				{
					btn.displayString = I18n.format(LootRegistry.lootGroups.get(n3).name);
					btn.enabled = btn.visible = true;
				} else
				{
					btn.displayString = "NULL";
					btn.enabled = btn.visible = false;
				}
			} else if(n2 == 1)
			{
				btn.enabled = btn.visible = n3 >= 0 && n3 < LootRegistry.lootGroups.size();
			}
		}
		
		if(selected == null)
		{
			lineTitle.setText("");
			lineTitle.setEnabled(false);
			lineWeight.setText("");
			lineWeight.setEnabled(false);
		} else
		{
			lineTitle.setText(selected.name);
			lineTitle.setEnabled(true);
			lineWeight.setText("" + selected.weight);
			lineWeight.setEnabled(true);
		}
	}
}
