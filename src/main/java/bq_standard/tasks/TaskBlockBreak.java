package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.placeholders.ItemPlaceholder;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.questing.tasks.IProgression;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.PanelLegacyEmbed;
import bq_standard.client.gui.tasks.GuiTaskBlockBreak;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskBlockBreak;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class TaskBlockBreak implements ITask, IProgression<int[]>
{
	private ArrayList<UUID> completeUsers = new ArrayList<UUID>();
	public HashMap<UUID, int[]> userProgress = new HashMap<UUID, int[]>();
	public ArrayList<JsonBlockType> blockTypes = new ArrayList<JsonBlockType>();
	
	public TaskBlockBreak()
	{
		blockTypes.add(new JsonBlockType());
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
		int[] progress = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? getPartyProgress(playerID) : getGlobalProgress();
		
		for(int j = 0; j < blockTypes.size(); j++)
		{
			JsonBlockType block = blockTypes.get(j);
			
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
			JsonBlockType block = blockTypes.get(i);
			
			boolean flag = block.oreDict.length() > 0 && OreDictionary.getOres(block.oreDict).contains(new ItemStack(state.getBlock(), 1, block.m < 0? OreDictionary.WILDCARD_VALUE : state.getBlock().getMetaFromState(state)));
			
			if((flag || (state.getBlock() == block.b && (block.m < 0 || state.getBlock().getMetaFromState(state) == block.m))) && ItemComparison.CompareNBTTag(block.tags, tags, true))
			{
				progress[i] += 1;
				setUserProgress(player.getUniqueID(), progress);
				break;
			}
		}
		
		detect(player, quest);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.PROGRESS)
		{
			return this.writeProgressToJson(json);
		} else if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		NBTTagList bAry = new NBTTagList();
		for(JsonBlockType block : blockTypes)
		{
			NBTTagCompound jbt = block.writeToJson(new NBTTagCompound());
			bAry.appendTag(jbt);
		}
		json.setTag("blocks", bAry);
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.PROGRESS)
		{
			this.readProgressFromJson(json);
			return;
		} else if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		blockTypes.clear();
		NBTTagList bList = json.getTagList("blocks", 10);
		for(int i = 0; i < bList.tagCount(); i++)
		{
			NBTBase element = bList.get(i);
			
			if(element == null || element.getId() != 10)
			{
				continue;
			}
			
			JsonBlockType block = new JsonBlockType();
			block.readFromJson((NBTTagCompound)element);
			blockTypes.add(block);
		}
		
		if(json.hasKey("blockID", 8))
		{
			Block targetBlock = (Block)Block.REGISTRY.getObject(new ResourceLocation(json.getString("blockID")));
			targetBlock = targetBlock != null? targetBlock : Blocks.LOG;
			int targetMeta = json.getInteger("blockMeta");
			NBTTagCompound targetNbt = json.getCompoundTag("blockNBT");
			int targetNum = json.getInteger("amount");
			
			JsonBlockType leg = new JsonBlockType();
			leg.b = targetBlock;
			leg.m = targetMeta;
			leg.tags = targetNbt;
			leg.n = targetNum;
			
			blockTypes.add(leg);
		}
	}
	
	public void readProgressFromJson(NBTTagCompound json)
	{
		completeUsers = new ArrayList<UUID>();
		NBTTagList cList = json.getTagList("completeUsers", 8);
		for(int i = 0; i < cList.tagCount(); i++)
		{
			NBTBase entry = cList.get(i);
			
			if(entry == null || entry.getId() != 8)
			{
				continue;
			}
			
			try
			{
				completeUsers.add(UUID.fromString(((NBTTagString)entry).getString()));
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load UUID for task", e);
			}
		}
		
		userProgress = new HashMap<UUID,int[]>();
		NBTTagList pList = json.getTagList("userProgress", 10);
		for(int n = 0; n < pList.tagCount(); n++)
		{
			NBTBase entry = pList.get(n);
			
			if(entry == null || entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound pTag = (NBTTagCompound)entry;
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
	
	public NBTTagCompound writeProgressToJson(NBTTagCompound json)
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
		userProgress = new HashMap<UUID, int[]>();
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
			JsonBlockType block = blockTypes.get(i);
			total += progress[i] / (float)block.n;
		}
		
		return total / (float)blockTypes.size();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiPanel getTaskGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new PanelLegacyEmbed<>(new GuiRectangle(posX, posY, sizeX, sizeY), new GuiTaskBlockBreak(this, quest, posX, posY, sizeX, sizeY));
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
		
		return progress == null || progress.length != blockTypes.size()? new int[blockTypes.size()] : progress;
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
	
	public static class JsonBlockType
	{
		public Block b = Blocks.LOG;
		public int m = -1;
		public NBTTagCompound tags = new NBTTagCompound();
		public int n = 1;
		public String oreDict = "";
		
		public NBTTagCompound writeToJson(NBTTagCompound json)
		{
			json.setString("blockID", b.getRegistryName().toString());
			json.setInteger("meta", m);
			json.setTag("nbt", tags);
			json.setInteger("amount", n);
			json.setString("oreDict", oreDict);
			return json;
		}
		
		public void readFromJson(NBTTagCompound json)
		{
			b = (Block)Block.REGISTRY.getObject(new ResourceLocation(json.getString("blockID")));
			b = b != null? b : Blocks.LOG;
			m = json.getInteger("meta");
			n = n < 0? OreDictionary.WILDCARD_VALUE : n;
			tags = json.getCompoundTag("nbt");
			n = json.getInteger("amount");
			oreDict = json.getString("oreDict");
		}
		
		public BigItemStack getItemStack()
		{
			BigItemStack stack = null;
			
			if(b == null || Item.getItemFromBlock(b) == null)
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

	@Override
	public IJsonDoc getDocumentation()
	{
		return null;
	}
}
