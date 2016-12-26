package bq_standard.handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import bq_standard.client.gui.editors.GuiLootGroupEditor;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}
	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if(ID == 0)
		{
			return new GuiLootGroupEditor(null);
		}
		
		return null;
	}
}
