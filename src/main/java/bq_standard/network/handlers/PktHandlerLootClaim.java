package bq_standard.network.handlers;

import betterquesting.api.network.IPacketHandler;
import betterquesting.api.utils.BigItemStack;
import bq_standard.client.gui2.GuiLootChest;
import bq_standard.network.StandardPacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

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
		List<BigItemStack> rewards = new ArrayList<>();
		
		NBTTagList list = data.getTagList("rewards", 10);
		
		for(int i = 0; i < list.tagCount(); i++)
		{
			rewards.add(BigItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i)));
		}
		
		Minecraft.getMinecraft().displayGuiScreen(new GuiLootChest(null, rewards, title));
	}

	@Override
	public ResourceLocation getRegistryName()
	{
		return StandardPacketType.LOOT_CLAIM.GetLocation();
	}
}
