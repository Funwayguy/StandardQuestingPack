package bq_standard.network.handlers;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.network.handlers.PktHandler;
import betterquesting.utils.BigItemStack;
import bq_standard.client.gui.GuiLootChest;

public class PktHandlerLootClaim extends PktHandler
{
	@Override
	public void handleServer(EntityPlayerMP sender, NBTTagCompound data)
	{
	}
	
	@Override
	public void handleClient(NBTTagCompound data)
	{
		String title = data.getString("title");
		ArrayList<BigItemStack> rewards = new ArrayList<BigItemStack>();
		
		NBTTagList list = data.getTagList("rewards", 10);
		
		for(int i = 0; i < list.tagCount(); i++)
		{
			BigItemStack stack = BigItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
			
			if(stack != null)
			{
				rewards.add(stack);
			}
		}
		
		Minecraft.getMinecraft().displayGuiScreen(new GuiLootChest(rewards, title));
	}
}
