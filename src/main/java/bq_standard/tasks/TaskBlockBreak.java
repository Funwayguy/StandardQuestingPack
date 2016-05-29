package bq_standard.tasks;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyManager;
import betterquesting.party.PartyInstance.PartyMember;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.advanced.AdvancedTaskBase;
import betterquesting.quests.tasks.advanced.IProgressionTask;
import betterquesting.utils.ItemComparison;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import bq_standard.client.gui.tasks.GuiTaskBlockBreak;
import bq_standard.core.BQ_Standard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TaskBlockBreak extends AdvancedTaskBase implements IProgressionTask<Integer>
{
	public HashMap<UUID, Integer> userProgress = new HashMap<UUID, Integer>();
	public Block targetBlock = Blocks.LOG;
	public NBTTagCompound targetNbt = new NBTTagCompound();
	public int targetMeta = -1;
	public int targetNum = 1;
	public boolean oreDict = true;
	
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
		
		int progress = quest == null || !quest.globalQuest? GetPartyProgress(player.getUniqueID()) : GetGlobalProgress();
		
		if(progress >= targetNum)
		{
			setCompletion(player.getUniqueID(), true);
		}
	}
	
	@Override
	public void onBlockBreak(QuestInstance quest, EntityPlayer player, IBlockState state, BlockPos pos)
	{
		if(isComplete(player.getUniqueID()))
		{
			return;
		}
		
		int progress = GetUserProgress(player.getUniqueID());
		TileEntity tile = player.worldObj.getTileEntity(pos);
		NBTTagCompound tags = new NBTTagCompound();
		
		if(tile != null)
		{
			tile.writeToNBT(tags);
		}
		
		if(state.getBlock() == targetBlock && (targetMeta < 0 || state.getBlock().getMetaFromState(state) == targetMeta) && ItemComparison.CompareNBTTag(targetNbt, tags, true))
		{
			SetUserProgress(player.getUniqueID(), progress + 1);
		}
		
		Detect(quest, player);
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("blockID", Block.REGISTRY.getNameForObject(targetBlock).toString());
		json.addProperty("blockMeta", targetMeta);
		json.add("blockNBT", NBTConverter.NBTtoJSON_Compound(targetNbt, new JsonObject()));
		json.addProperty("amount", targetNum);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
		targetBlock = (Block)Block.REGISTRY.getObject(new ResourceLocation(JsonHelper.GetString(json, "blockID", "minecraft:log")));
		targetBlock = targetBlock != null? targetBlock : Blocks.LOG;
		targetMeta = JsonHelper.GetNumber(json, "blockMeta", -1).intValue();
		targetNbt = NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(json, "blockNBT"), new NBTTagCompound());
		targetNum = JsonHelper.GetNumber(json, "amount", 1).intValue();
		
		if(json.has("userProgress"))
		{
			jMig = json;
		}
	}
	
	JsonObject jMig = null;
	
	@Override
	public void readProgressFromJson(JsonObject json)
	{
		super.readProgressFromJson(json);
		
		if(jMig != null)
		{
			json = jMig;
			jMig = null;
		}
		
		userProgress = new HashMap<UUID,Integer>();
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
			
			userProgress.put(uuid, JsonHelper.GetNumber(entry.getAsJsonObject(), "value", 0).intValue());
		}
	}
	
	@Override
	public void writeProgressToJson(JsonObject json)
	{
		super.writeProgressToJson(json);
		
		JsonArray progArray = new JsonArray();
		for(Entry<UUID,Integer> entry : userProgress.entrySet())
		{
			JsonObject pJson = new JsonObject();
			pJson.addProperty("uuid", entry.getKey().toString());
			pJson.addProperty("value", entry.getValue());
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
		userProgress = new HashMap<UUID, Integer>();
	}
	
	@Override
	public float GetParticipation(UUID uuid)
	{
		if(targetNum <= 0)
		{
			return 1F;
		}
		
		return GetUserProgress(uuid) / (float)targetNum;
	}

	@Override
	public GuiEmbedded getGui(QuestInstance quest, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskBlockBreak(quest, this, screen, posX, posY, sizeX, sizeY);
	}
	
	@Override
	public void SetUserProgress(UUID uuid, Integer progress)
	{
		userProgress.put(uuid, progress);
	}
	
	@Override
	public Integer GetUserProgress(UUID uuid)
	{
		Integer i = userProgress.get(uuid);
		return i == null? 0 : i;
	}

	@Override
	public Integer GetPartyProgress(UUID uuid)
	{
		int total = 0;
		
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
				
				total += GetUserProgress(mem.userID);
			}
		}
		
		return total;
	}
	
	@Override
	public Integer GetGlobalProgress()
	{
		int total = 0;
		
		for(Integer i : userProgress.values())
		{
			total += i == null? 0 : 1;
		}
		
		return total;
	}
}
