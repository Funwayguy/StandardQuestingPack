package bq_standard.tasks;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.Level;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.tasks.advanced.AdvancedTaskBase;
import betterquesting.utils.JsonHelper;
import bq_standard.client.gui.tasks.GuiTaskBlock;
import bq_standard.core.BQ_Standard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TaskBlockBreak extends AdvancedTaskBase
{
	public HashMap<UUID, Integer> userProgress = new HashMap<UUID, Integer>();
	public Block targetBlock = Blocks.log;
	public int targetMeta = -1;
	public int targetNum = 1;
	public boolean oreDict = true;
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.block_break";
	}
	
	@Override
	public void Update(EntityPlayer player)
	{
		if(player.ticksExisted%200 == 0)
		{
			Detect(player);
		}
	}
	
	@Override
	public void Detect(EntityPlayer player)
	{
		if(isComplete(player.getUniqueID()))
		{
			return;
		}
		
		Integer progress = userProgress.get(player.getUniqueID());
		progress = progress == null? 0 : progress;
		
		if(progress >= targetNum)
		{
			setCompletion(player.getUniqueID(), true);
		}
	}
	
	@Override
	public void onBlockBreak(EntityPlayer player, IBlockState state, BlockPos pos)
	{
		if(isComplete(player.getUniqueID()))
		{
			return;
		}
		
		Integer progress = userProgress.get(player.getUniqueID());
		progress = progress == null? 0 : progress;
		
		if(state.getBlock() == targetBlock && (targetMeta < 0 || state.getBlock().getMetaFromState(state) == targetMeta))
		{
			progress++;
			userProgress.put(player.getUniqueID(), progress);
			
			if(progress >= targetNum)
			{
				setCompletion(player.getUniqueID(), true);
			}
		}
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("blockID", Block.blockRegistry.getNameForObject(targetBlock).toString());
		json.addProperty("blockMeta", targetMeta);
		json.addProperty("amount", targetNum);
		
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
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
		targetBlock = (Block)Block.blockRegistry.getObject(new ResourceLocation(JsonHelper.GetString(json, "blockID", "minecraft:log")));
		targetBlock = targetBlock != null? targetBlock : Blocks.log;
		targetMeta = JsonHelper.GetNumber(json, "blockMeta", -1).intValue();
		targetNum = JsonHelper.GetNumber(json, "amount", 1).intValue();
		
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
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskBlock(this, screen, posX, posY, sizeX, sizeY);
	}
}
