package bq_standard.network.handlers;

import betterquesting.api.network.IPacketHandler;
import betterquesting.api.utils.BigItemStack;
import bq_standard.client.gui.GuiLootChest;
import bq_standard.network.StandardPacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class PktHandlerLootClaim implements IPacketHandler
{
	@Override
	public void handleServer(NBTTagCompound data, EntityPlayerMP sender)
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

	@Override
	public ResourceLocation getRegistryName()
	{
		return StandardPacketType.LOOT_CLAIM.GetLocation();
	}
}
