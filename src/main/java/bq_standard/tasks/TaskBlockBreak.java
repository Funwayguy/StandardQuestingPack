package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.placeholders.ItemPlaceholder;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.IProgression;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import bq_standard.client.gui.tasks.PanelTaskBlockBreak;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskBlockBreak;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
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
		
		if(isComplete(playerID))
		{
			return;
		}
		
		boolean flag = true;
		int[] progress = quest == null || !quest.getProperty(NativeProps.GLOBAL)? getPartyProgress(playerID) : getGlobalProgress();
		
		for(int j = 0; j < blockTypes.size(); j++)
		{
			NbtBlockType block = blockTypes.get(j);
			
			if(block == null || progress[j] >= block.n)
			{
				continue;
			}
			
			flag = false;
			break;
		}
		
		if(flag)
		{
			setComplete(playerID);
		}
	}
	
	public void onBlockBreak(IQuest quest, EntityPlayer player, IBlockState state, BlockPos pos)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		if(isComplete(playerID))
		{
			return;
		}
		
		int[] progress = getUsersProgress(playerID);
		TileEntity tile = player.world.getTileEntity(pos);
		NBTTagCompound tags = new NBTTagCompound();
		
		if(tile != null)
		{
			tile.writeToNBT(tags);
		}
		
		for(int i = 0; i < blockTypes.size(); i++)
		{
			NbtBlockType block = blockTypes.get(i);
			
			boolean flag = block.oreDict.length() > 0 && OreDictionary.getOres(block.oreDict).contains(new ItemStack(state.getBlock(), 1, block.m < 0? OreDictionary.WILDCARD_VALUE : state.getBlock().getMetaFromState(state)));
			
			if((flag || (state.getBlock() == block.b && (block.m < 0 || state.getBlock().getMetaFromState(state) == block.m))) && ItemComparison.CompareNBTTag(block.tags, tags, true))
			{
				progress[i] += 1;
				setUserProgress(player.getUniqueID(), progress);
                QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
                if(qc != null) qc.markQuestDirty(QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
				break;
			}
		}
		
		detect(player, quest);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json)
	{
		NBTTagList bAry = new NBTTagList();
		for(NbtBlockType block : blockTypes)
		{
			NBTTagCompound jbt = block.writeToNBT(new NBTTagCompound());
			bAry.appendTag(jbt);
		}
		json.setTag("blocks", bAry);
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json)
	{
		blockTypes.clear();
		NBTTagList bList = json.getTagList("blocks", 10);
		for(int i = 0; i < bList.tagCount(); i++)
		{
			NbtBlockType block = new NbtBlockType();
			block.readFromNBT(bList.getCompoundTagAt(i));
			blockTypes.add(block);
		}
		
		if(json.hasKey("blockID", 8))
		{
			Block targetBlock = Block.REGISTRY.getObject(new ResourceLocation(json.getString("blockID")));
			targetBlock = targetBlock != Blocks.AIR ? targetBlock : Blocks.LOG;
			int targetMeta = json.getInteger("blockMeta");
			NBTTagCompound targetNbt = json.getCompoundTag("blockNBT");
			int targetNum = json.getInteger("amount");
			
			NbtBlockType leg = new NbtBlockType();
			leg.b = targetBlock;
			leg.m = targetMeta;
			leg.tags = targetNbt;
			leg.n = targetNum;
			
			blockTypes.add(leg);
		}
	}
	
	@Override
	public void readProgressFromNBT(NBTTagCompound json, boolean merge)
	{
		completeUsers.clear();
		NBTTagList cList = json.getTagList("completeUsers", 8);
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
		NBTTagList pList = json.getTagList("userProgress", 10);
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
			NBTTagList dJson = pTag.getTagList("data", 3);
			for(int i = 0; i < data.length && i < dJson.tagCount(); i++)
			{
				try
				{
					data[i] = dJson.getIntAt(i);
				} catch(Exception e)
				{
					BQ_Standard.logger.log(Level.ERROR, "Incorrect task progress format", e);
				}
			}
			
			userProgress.put(uuid, data);
		}
	}
	
	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound json, List<UUID> users)
	{
		NBTTagList jArray = new NBTTagList();
		for(UUID uuid : completeUsers)
		{
			jArray.appendTag(new NBTTagString(uuid.toString()));
		}
		json.setTag("completeUsers", jArray);
		
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
		json.setTag("userProgress", progArray);
		
		return json;
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
			
			if(tmp == null || tmp.length != blockTypes.size())
			{
				continue;
			}
			
			for(int n = 0; n < progress.length; n++)
			{
				progress[n] += tmp[n];
			}
		}
		
		return progress.length != blockTypes.size()? new int[blockTypes.size()] : progress;
	}
	
	public int[] getPartyProgress(UUID uuid)
	{
		int[] total = new int[blockTypes.size()];
		
		IParty party = QuestingAPI.getAPI(ApiReference.PARTY_DB).getUserParty(uuid);
		
		if(party == null)
		{
			return getUsersProgress(uuid);
		} else
		{
			for(UUID mem : party.getMembers())
			{
				if(mem != null && party.getStatus(mem).ordinal() <= 0)
				{
					continue;
				}

				int[] progress = getUsersProgress(mem);
				
				for(int i = 0; i < progress.length; i++)
				{
					total[i] += progress[i];
				}
			}
		}
		
		return total;
	}
	
	@Override
	public int[] getGlobalProgress()
	{
		int[] total = new int[blockTypes.size()];
		
		for(int[] up : userProgress.values())
		{
			if(up == null)
			{
				continue;
			}
			
			int[] progress = up.length != blockTypes.size()? new int[blockTypes.size()] : up;
			
			for(int i = 0; i < progress.length; i++)
			{
				total[i] += progress[i];
			}
		}
		
		return total;
	}
	
	public static class NbtBlockType
	{
		public Block b = Blocks.LOG;
		public int m = -1;
		public NBTTagCompound tags = new NBTTagCompound();
		public int n = 1;
		public String oreDict = "";
		
		public NBTTagCompound writeToNBT(NBTTagCompound json)
		{
			json.setString("blockID", b.getRegistryName().toString());
			json.setInteger("meta", m);
			json.setTag("nbt", tags);
			json.setInteger("amount", n);
			json.setString("oreDict", oreDict);
			return json;
		}
		
		public void readFromNBT(NBTTagCompound json)
		{
			b = Block.REGISTRY.getObject(new ResourceLocation(json.getString("blockID")));
			b = b != Blocks.AIR? b : Blocks.LOG;
			m = json.getInteger("meta");
			n = n < 0? OreDictionary.WILDCARD_VALUE : n;
			tags = json.getCompoundTag("nbt");
			n = json.getInteger("amount");
			oreDict = json.getString("oreDict");
		}
		
		public BigItemStack getItemStack()
		{
			BigItemStack stack;
			
			if(b == null || Item.getItemFromBlock(b) == Items.AIR)
			{
				stack = new BigItemStack(ItemPlaceholder.placeholder, n, 0);
				stack.getBaseStack().setStackDisplayName("NULL");
				
				if(b != null)
				{
					stack.getBaseStack().setStackDisplayName(b.getLocalizedName());
					stack.GetTagCompound().setString("orig_id", Block.REGISTRY.getNameForObject(b).toString());
				} else
				{
					stack.getBaseStack().setStackDisplayName("NULL");
					stack.GetTagCompound().setString("orig_id", "NULL");
				}
			} else
			{
				stack = new BigItemStack(b, n, m);
			}
			
			stack.oreDict = oreDict;
			return stack;
		}
	}
}
