package bq_standard.client.gui.tasks;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.properties.NativeProps;
import betterquesting.api.utils.RenderUtils;
import bq_standard.XPHelper;
import bq_standard.tasks.TaskXP;

public class GuiTaskXP extends GuiElement implements IGuiEmbedded
{
	private ItemStack bottle = new ItemStack(Items.experience_bottle);
	private IQuest quest;
	private TaskXP task;
	private Minecraft mc;
	
	private int posX = 0;
	private int posY = 0;
	private int sizeX = 0;
	private int sizeY = 0;
	
	public GuiTaskXP(TaskXP task, IQuest quest, int posX, int posY, int sizeX, int sizeY)
	{
		this.mc = Minecraft.getMinecraft();
		this.task = task;
		this.quest = quest;
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}
	
	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		int barSize = Math.min(sizeX/2, 128);
		int xp = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? task.getPartyProgress(mc.thePlayer.getUniqueID()) : task.getGlobalProgress();
		xp = !task.levels? xp : XPHelper.getXPLevel(xp);
		int barProg = (int)(MathHelper.clamp_float(xp/(float)task.amount, 0F, 1F) * (barSize - 2));
		int midX = sizeX/2;
		
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glScalef(2F, 2F, 2F);
		RenderUtils.RenderItemStack(mc, bottle, (posX + sizeX/2 - 16)/2, (posY + sizeY/2 - 32)/2, "");
		GL11.glPopMatrix();
		drawRect(posX + midX - barSize/2, posY + sizeY/2, posX + midX + barSize/2, posY + sizeY/2 + 16, Color.BLACK.getRGB());
		drawRect(posX + midX - barSize/2 + 1, posY + sizeY/2 + 1, posX + midX - barSize/2 + barProg + 1, posY + sizeY/2 + 15, Color.GREEN.getRGB());
		String txt = EnumChatFormatting.BOLD + "" + xp + "/" + task.amount + (task.levels? "L" : "XP");
		mc.fontRenderer.drawString(txt, posX + sizeX/2 - mc.fontRenderer.getStringWidth(txt)/2, posY + sizeY/2 + 4, Color.WHITE.getRGB(), true);
	}

	@Override
	public void drawForeground(int mx, int my, float partialTick)
	{
	}

	@Override
	public void onMouseClick(int mx, int my, int click)
	{
	}

	@Override
	public void onMouseScroll(int mx, int my, int scroll)
	{
	}

	@Override
	public void onKeyTyped(char c, int keyCode)
	{
	}
}
