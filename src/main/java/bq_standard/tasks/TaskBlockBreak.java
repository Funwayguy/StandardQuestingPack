package bq_standard.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.advanced.AdvancedTaskBase;
import betterquesting.quests.tasks.advanced.IProgressionTask;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.ItemComparison;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import bq_standard.client.gui.tasks.GuiTaskBlockBreak;
import bq_standard.core.BQ_Standard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class TaskBlockBreak extends AdvancedTaskBase implements IProgressionTask<int[]>
{
	public HashMap<UUID, int[]> userProgress = new HashMap<UUID, int[]>();
	public ArrayList<JsonBlockType> blockTypes = new ArrayList<JsonBlockType>();
	
	public TaskBlockBreak()
	{
		blockTypes.add(new JsonBlockType());
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.block_break";
	}
	
	@Override
	public void Update(QuestInstance quest, EntityPlayer player)
	{
		if(player.ticksExisted%200 == 0 && !QuestDatabase.editMode)
		{
			Detect(quest, player);
		}
	}
	
	@Override
	public void Detect(QuestInstance quest, EntityPlayer player)
	{
		if(isComplete(player.getUniqueID()))
		{
			return;
		}
		
		boolean flag = false;
		int[] progress = quest == null || !quest.globalQuest? GetPartyProgress(player.getUniqueID()) : GetGlobalProgress();
		
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
			setCompletion(player.getUniqueID(), true);
		}
	}
	
	@Override
	public void onBlockBreak(QuestInstance quest, EntityPlayer player, Block b, int metadata, int x, int y, int z)
	{
		if(isComplete(player.getUniqueID()))
		{
			return;
		}
		
		int[] progress = GetUserProgress(player.getUniqueID());
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
				SetUserProgress(player.getUniqueID(), progress);
				break;
			}
		}
		
		Detect(quest, player);
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		JsonArray bAry = new JsonArray();
		for(JsonBlockType block : blockTypes)
		{
			JsonObject jbt = block.writeToJson(new JsonObject());
			bAry.add(jbt);
		}
		json.add("blocks", bAry);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
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
		
		if(json.has("userProgress"))
		{
			jMig = json;
		}
	}
	
	JsonObject jMig = null; // Used for migrating progress over
	
	@Override
	public void readProgressFromJson(JsonObject json)
	{
		super.readProgressFromJson(json);
		
		if(jMig != null)
		{
			json = jMig;
			jMig = null;
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
	
	@Override
	public void writeProgressToJson(JsonObject json)
	{
		super.writeProgressToJson(json);
		
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
	}
	
	@Override
	public void ResetProgress(UUID uuid)
	{
		super.ResetProgress(uuid);
		userProgress.remove(uuid);
	}

	@Override
	public void ResetAllProgress()
	{
		super.ResetAllProgress();
		userProgress = new HashMap<UUID, int[]>();
	}
	
	@Override
	public float GetParticipation(UUID uuid)
	{
		if(blockTypes.size() <= 0)
		{
			return 1F;
		}
		
		float total = 0F;
		
		int[] progress = GetUserProgress(uuid);
		for(int i = 0; i < blockTypes.size(); i++)
		{
			JsonBlockType block = blockTypes.get(i);
			total += progress[i] / (float)block.n;
		}
		
		return total / (float)blockTypes.size();
	}

	@Override
	public GuiEmbedded getGui(QuestInstance quest, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskBlockBreak(quest, this, screen, posX, posY, sizeX, sizeY);
	}
	
	@Override
	public void SetUserProgress(UUID uuid, int[] progress)
	{
		userProgress.put(uuid, progress);
	}
	
	@Override
	public int[] GetUserProgress(UUID uuid)
	{
		int[] progress = userProgress.get(uuid);
		return progress == null || progress.length != blockTypes.size()? new int[blockTypes.size()] : progress;
	}
	
	@Override
	public int[] GetPartyProgress(UUID uuid)
	{
		int[] total = new int[blockTypes.size()];
		
		PartyInstance party = PartyManager.GetParty(uuid);
		
		if(party == null)
		{
			return GetUserProgress(uuid);
		} else
		{
			for(PartyMember mem : party.GetMembers())
			{
				if(mem != null && mem.GetPrivilege() <= 0)
				{
					continue;
				}

				int[] progress = GetUserProgress(mem.userID);
				
				for(int i = 0; i < progress.length; i++)
				{
					total[i] += progress[i];
				}
			}
		}
		
		return total;
	}
	
	@Override
	public int[] GetGlobalProgress()
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
			m = JsonHelper.GetNumber(json, "meta", -1).intValue();
			n = n < 0? OreDictionary.WILDCARD_VALUE : n;
			tags = NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(json, "nbt"), new NBTTagCompound(), true);
			n = JsonHelper.GetNumber(json, "amount", 1).intValue();
			oreDict = JsonHelper.GetString(json, "oreDict", "");
		}
		
		public BigItemStack getItemStack()
		{
			BigItemStack stack = new BigItemStack(b, n < 0? OreDictionary.WILDCARD_VALUE : n, m);
			stack.oreDict = oreDict;
			return stack;
		}
	}
}
