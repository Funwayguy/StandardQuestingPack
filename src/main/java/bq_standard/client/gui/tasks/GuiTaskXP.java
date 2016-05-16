package bq_standard.client.gui.tasks;

import java.awt.Color;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.RenderUtils;
import bq_standard.XPHelper;
import bq_standard.tasks.TaskXP;

public class GuiTaskXP extends GuiEmbedded
{
	ItemStack bottle = new ItemStack(Items.experience_bottle);
	QuestInstance quest;
	TaskXP task;
	
	public GuiTaskXP(QuestInstance quest, TaskXP task, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		this.task = task;
		this.quest = quest;
	}
	
	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		int barSize = Math.min(sizeX/2, 128);
		int xp = quest == null || !quest.globalQuest? task.GetPartyProgress(screen.mc.thePlayer.getUniqueID()) : task.GetGlobalProgress();
		xp = !task.levels? xp : XPHelper.getXPLevel(xp);
		int barProg = (int)(MathHelper.clamp_float(xp/(float)task.amount, 0F, 1F) * (barSize - 2));
		int midX = sizeX/2;
		
		GlStateManager.pushMatrix();
		GlStateManager.enableDepth();
		GlStateManager.scale(2F, 2F, 2F);
		RenderUtils.RenderItemStack(screen.mc, bottle, (posX + sizeX/2 - 16)/2, (posY + sizeY/2 - 32)/2, "");
		GlStateManager.popMatrix();
		GuiQuesting.drawRect(posX + midX - barSize/2, posY + sizeY/2, posX + midX + barSize/2, posY + sizeY/2 + 16, Color.BLACK.getRGB());
		GuiQuesting.drawRect(posX + midX - barSize/2 + 1, posY + sizeY/2 + 1, posX + midX - barSize/2 + barProg + 1, posY + sizeY/2 + 15, Color.GREEN.getRGB());
		String txt = TextFormatting.BOLD + "" + xp + "/" + task.amount + (task.levels? "L" : "XP");
		screen.mc.fontRendererObj.drawString(txt, posX + sizeX/2 - screen.mc.fontRendererObj.getStringWidth(txt)/2, posY + sizeY/2 + 4, Color.WHITE.getRGB(), true);
	}
	
	/*
	 * Bottle Icon
	 * Progress Text & Bar
	 */
}
