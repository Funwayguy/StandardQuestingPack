package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.IProgression;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import bq_standard.NbtBlockType;
import bq_standard.client.gui.tasks.PanelTaskBlockBreak;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskBlockBreak;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class TaskBlockBreak implements ITask, IProgression<int[]>
{
	private final List<UUID> completeUsers = new ArrayList<>();
	private final HashMap<UUID, int[]> userProgress = new HashMap<>();
	public final List<NbtBlockType> blockTypes = new ArrayList<>();
	
	public TaskBlockBreak()
	{
		blockTypes.add(new NbtBlockType());
	}
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskBlockBreak.INSTANCE.getRegistryName();
	}
	
	@Override
	public boolean isComplete(UUID uuid)
	{
		return completeUsers.contains(uuid);
	}
	
	@Override
	public void setComplete(UUID uuid)
	{
		if(!completeUsers.contains(uuid))
		{
			completeUsers.add(uuid);
		}
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.block_break";
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		if(isComplete(playerID)) return;
		
		int[] progress = quest == null || !quest.getProperty(NativeProps.GLOBAL)? getPartyProgress(playerID) : getGlobalProgress();
		
		for(int j = 0; j < blockTypes.size(); j++)
		{
			NbtBlockType block = blockTypes.get(j);
			
			if(block == null || progress[j] >= block.n) continue;
			return;
		}
		
        setComplete(playerID);
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        if(qc != null) qc.markQuestDirty(QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
	}
	
	public void onBlockBreak(DBEntry<IQuest> quest, EntityPlayer player, IBlockState state, BlockPos pos)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		if(isComplete(playerID)) return;
		
		int[] progress = getUsersProgress(playerID);
		TileEntity tile = state.getBlock().hasTileEntity(state) ? player.worldObj.getTileEntity(pos) : null;
		NBTTagCompound tags = tile == null ? null : tile.writeToNBT(new NBTTagCompound());
		
		for(int i = 0; i < blockTypes.size(); i++)
		{
			NbtBlockType block = blockTypes.get(i);
			if(progress[i] >= block.n) continue;
			
			boolean oreMatch = block.oreDict.length() > 0 && OreDictionary.getOres(block.oreDict).contains(new ItemStack(state.getBlock(), 1, block.m < 0? OreDictionary.WILDCARD_VALUE : state.getBlock().getMetaFromState(state)));
			
			if((oreMatch || (state.getBlock() == block.b && (block.m < 0 || state.getBlock().getMetaFromState(state) == block.m))) && ItemComparison.CompareNBTTag(block.tags, tags, true))
			{
				progress[i]++;
				setUserProgress(player.getUniqueID(), progress);
                QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
                if(qc != null) qc.markQuestDirty(quest.getID());
				break; // NOTE: We're only tracking one break at a time so doing all the progress setting above is fine
			}
		}
		
		detect(player, quest.getValue());
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList bAry = new NBTTagList();
		for(NbtBlockType block : blockTypes)
		{
			bAry.appendTag(block.writeToNBT(new NBTTagCompound()));
		}
		nbt.setTag("blocks", bAry);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		blockTypes.clear();
		NBTTagList bList = nbt.getTagList("blocks", 10);
		for(int i = 0; i < bList.tagCount(); i++)
		{
			NbtBlockType block = new NbtBlockType();
			block.readFromNBT(bList.getCompoundTagAt(i));
			blockTypes.add(block);
		}
		
		if(nbt.hasKey("blockID", 8))
		{
			Block targetBlock = Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("blockID")));
			targetBlock = targetBlock != Blocks.AIR ? targetBlock : Blocks.LOG;
			int targetMeta = nbt.getInteger("blockMeta");
			NBTTagCompound targetNbt = nbt.getCompoundTag("blockNBT");
			int targetNum = nbt.getInteger("amount");
			
			NbtBlockType leg = new NbtBlockType();
			leg.b = targetBlock;
			leg.m = targetMeta;
			leg.tags = targetNbt;
			leg.n = targetNum;
			
			blockTypes.add(leg);
		}
	}
	
	@Override
	public void readProgressFromNBT(NBTTagCompound nbt, boolean merge)
	{
		completeUsers.clear();
		NBTTagList cList = nbt.getTagList("completeUsers", 8);
		for(int i = 0; i < cList.tagCount(); i++)
		{
			try
			{
				completeUsers.add(UUID.fromString(cList.getStringTagAt(i)));
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load UUID for task", e);
			}
		}
		
		userProgress.clear();
		NBTTagList pList = nbt.getTagList("userProgress", 10);
		for(int n = 0; n < pList.tagCount(); n++)
		{
			NBTTagCompound pTag = pList.getCompoundTagAt(n);
			UUID uuid;
			try
			{
				uuid = UUID.fromString(pTag.getString("uuid"));
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load user progress for task", e);
				continue;
			}
			
			int[] data = new int[blockTypes.size()];
			NBTTagList dNbt = pTag.getTagList("data", 3);
			for(int i = 0; i < data.length && i < dNbt.tagCount(); i++)
			{
				try
				{
					data[i] = dNbt.getIntAt(i);
				} catch(Exception e)
				{
					BQ_Standard.logger.log(Level.ERROR, "Incorrect task progress format", e);
				}
			}
			
			userProgress.put(uuid, data);
		}
	}
	
	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, List<UUID> users)
	{
		NBTTagList jArray = new NBTTagList();
		for(UUID uuid : completeUsers)
		{
			jArray.appendTag(new NBTTagString(uuid.toString()));
		}
		nbt.setTag("completeUsers", jArray);
		
		NBTTagList progArray = new NBTTagList();
		for(Entry<UUID,int[]> entry : userProgress.entrySet())
		{
			NBTTagCompound pJson = new NBTTagCompound();
			pJson.setString("uuid", entry.getKey().toString());
			NBTTagList pArray = new NBTTagList();
			for(int i : entry.getValue())
			{
				pArray.appendTag(new NBTTagInt(i));
			}
			pJson.setTag("data", pArray);
			progArray.appendTag(pJson);
		}
		nbt.setTag("userProgress", progArray);
		
		return nbt;
	}
	
	@Override
	public void resetUser(UUID uuid)
	{
		completeUsers.remove(uuid);
		userProgress.remove(uuid);
	}
	
	public void resetAll()
	{
		completeUsers.clear();
		userProgress.clear();
	}
	
	public float getParticipation(UUID uuid)
	{
		if(blockTypes.size() <= 0)
		{
			return 1F;
		}
		
		float total = 0F;
		
		int[] progress = getUsersProgress(uuid);
		for(int i = 0; i < blockTypes.size(); i++)
		{
			NbtBlockType block = blockTypes.get(i);
			total += progress[i] / (float)block.n;
		}
		
		return total / (float)blockTypes.size();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
	{
	    return new PanelTaskBlockBreak(rect, quest, this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen screen, IQuest quest)
	{
		return null;
	}
	
	@Override
	public void setUserProgress(UUID uuid, int[] progress)
	{
		userProgress.put(uuid, progress);
	}
	
	@Override
	public int[] getUsersProgress(UUID... users)
	{
		int[] progress = new int[blockTypes.size()];
		
		for(UUID uuid : users)
		{
			int[] tmp = userProgress.get(uuid);
			
			if(tmp == null || tmp.length != blockTypes.size()) continue;
			
			for(int n = 0; n < progress.length; n++)
			{
				progress[n] += tmp[n];
			}
		}
		
		return progress.length != blockTypes.size()? new int[blockTypes.size()] : progress;
	}
	
	public int[] getPartyProgress(UUID uuid)
	{
		IParty party = QuestingAPI.getAPI(ApiReference.PARTY_DB).getUserParty(uuid);
        return getUsersProgress(party == null ? new UUID[]{uuid} : party.getMembers().toArray(new UUID[0]));
	}
	
	@Override
	public int[] getGlobalProgress()
	{
		int[] total = new int[blockTypes.size()];
		
		for(int[] up : userProgress.values())
		{
			if(up == null || up.length != total.length) continue;
			
			for(int i = 0; i < up.length; i++)
			{
				total[i] += up[i];
			}
		}
		
		return total;
	}
}
