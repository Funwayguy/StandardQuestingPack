package bq_standard.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
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
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import bq_standard.client.gui.tasks.GuiTaskBlockBreak;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskBlockBreak;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
	public void update(EntityPlayer player, IQuest quest)
	{
		if(player.ticksExisted%200 == 0 && !QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE))
		{
			detect(player, quest);
		}
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
	
	public void onBlockBreak(IQuest quest, EntityPlayer player, Block b, int metadata, int x, int y, int z)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		if(isComplete(playerID))
		{
			return;
		}
		
		int[] progress = getUsersProgress(playerID);
		TileEntity tile = player.worldObj.getTileEntity(x, y, z);
		NBTTagCompound tags = new NBTTagCompound();
		
		if(tile != null)
		{
			tile.writeToNBT(tags);
		}
		
		for(int i = 0; i < blockTypes.size(); i++)
		{
			JsonBlockType block = blockTypes.get(i);
			
			boolean flag = block.oreDict.length() > 0 && OreDictionary.getOres(block.oreDict).contains(new ItemStack(b, 1, block.m < 0? OreDictionary.WILDCARD_VALUE : metadata));
			
			if((flag || (b == block.b && (block.m < 0 || metadata == block.m))) && ItemComparison.CompareNBTTag(block.tags, tags, true))
			{
				progress[i] += 1;
				setUserProgress(playerID, progress);
				break;
			}
		}
		
		detect(player, quest);
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.PROGRESS)
		{
			return this.writeProgressToJson(json);
		} else if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		JsonArray bAry = new JsonArray();
		for(JsonBlockType block : blockTypes)
		{
			JsonObject jbt = block.writeToJson(new JsonObject());
			bAry.add(jbt);
		}
		json.add("blocks", bAry);
		
		return json;
	}
	
	@Override
	public void readFromJson(JsonObject json, EnumSaveType saveType)
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
		for(JsonElement element : JsonHelper.GetArray(json, "blocks"))
		{
			if(element == null || !element.isJsonObject())
			{
				continue;
			}
			
			JsonBlockType block = new JsonBlockType();
			block.readFromJson(element.getAsJsonObject());
			blockTypes.add(block);
		}
		
		if(json.has("blockID"))
		{
			Block targetBlock = (Block)Block.blockRegistry.getObject(JsonHelper.GetString(json, "blockID", "minecraft:log"));
			targetBlock = targetBlock != null? targetBlock : Blocks.log;
			int targetMeta = JsonHelper.GetNumber(json, "blockMeta", -1).intValue();
			NBTTagCompound targetNbt = NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(json, "blockNBT"), new NBTTagCompound(), true);
			int targetNum = JsonHelper.GetNumber(json, "amount", 1).intValue();
			
			JsonBlockType leg = new JsonBlockType();
			leg.b = targetBlock;
			leg.m = targetMeta;
			leg.tags = targetNbt;
			leg.n = targetNum;
			
			blockTypes.add(leg);
		}
	}
	
	public void readProgressFromJson(JsonObject json)
	{
		completeUsers = new ArrayList<UUID>();
		for(JsonElement entry : JsonHelper.GetArray(json, "completeUsers"))
		{
			if(entry == null || !entry.isJsonPrimitive())
			{
				continue;
			}
			
			try
			{
				completeUsers.add(UUID.fromString(entry.getAsString()));
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load UUID for task", e);
			}
		}
		
		userProgress = new HashMap<UUID,int[]>();
		for(JsonElement entry : JsonHelper.GetArray(json, "userProgress"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			UUID uuid;
			try
			{
				uuid = UUID.fromString(JsonHelper.GetString(entry.getAsJsonObject(), "uuid", ""));
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load user progress for task", e);
				continue;
			}
			
			int[] data = new int[blockTypes.size()];
			JsonArray dJson = JsonHelper.GetArray(entry.getAsJsonObject(), "data");
			for(int i = 0; i < data.length && i < dJson.size(); i++)
			{
				try
				{
					data[i] = dJson.get(i).getAsInt();
				} catch(Exception e)
				{
					BQ_Standard.logger.log(Level.ERROR, "Incorrect task progress format", e);
				}
			}
			
			userProgress.put(uuid, data);
		}
	}
	
	public JsonObject writeProgressToJson(JsonObject json)
	{
		JsonArray jArray = new JsonArray();
		for(UUID uuid : completeUsers)
		{
			jArray.add(new JsonPrimitive(uuid.toString()));
		}
		json.add("completeUsers", jArray);
		
		JsonArray progArray = new JsonArray();
		for(Entry<UUID,int[]> entry : userProgress.entrySet())
		{
			JsonObject pJson = new JsonObject();
			pJson.addProperty("uuid", entry.getKey().toString());
			JsonArray pArray = new JsonArray();
			for(int i : entry.getValue())
			{
				pArray.add(new JsonPrimitive(i));
			}
			pJson.add("data", pArray);
			progArray.add(pJson);
		}
		json.add("userProgress", progArray);
		
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
	public IGuiEmbedded getTaskGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new GuiTaskBlockBreak(this, quest, posX, posY, sizeX, sizeY);
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
		public Block b = Blocks.log;
		public int m = -1;
		public NBTTagCompound tags = new NBTTagCompound();
		public int n = 1;
		public String oreDict = "";
		
		public JsonObject writeToJson(JsonObject json)
		{
			json.addProperty("blockID", Block.blockRegistry.getNameForObject(b));
			json.addProperty("meta", m);
			json.add("nbt", NBTConverter.NBTtoJSON_Compound(tags, new JsonObject(), true));
			json.addProperty("amount", n);
			json.addProperty("oreDict", oreDict);
			return json;
		}
		
		public void readFromJson(JsonObject json)
		{
			b = (Block)Block.blockRegistry.getObject(JsonHelper.GetString(json, "blockID", "minecraft:log"));
			b = b != null? b : Blocks.log;
			m = JsonHelper.GetNumber(json, "meta", -1).intValue();
			n = n < 0? OreDictionary.WILDCARD_VALUE : n;
			tags = NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(json, "nbt"), new NBTTagCompound(), true);
			n = JsonHelper.GetNumber(json, "amount", 1).intValue();
			oreDict = JsonHelper.GetString(json, "oreDict", "");
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
					stack.GetTagCompound().setString("orig_id", Block.blockRegistry.getNameForObject(b));
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
