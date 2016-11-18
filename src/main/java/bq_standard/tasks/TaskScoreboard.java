package bq_standard.tasks;

import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreDummyCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.properties.NativeProps;
import betterquesting.api.quests.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import betterquesting.quests.QuestSettings;
import bq_standard.ScoreboardBQ;
import bq_standard.client.gui.editors.GuiScoreEditor;
import bq_standard.client.gui.tasks.GuiTaskScoreboard;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskScoreboard;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TaskScoreboard implements ITask
{
	private ArrayList<UUID> completeUsers = new ArrayList<UUID>();
	public String scoreName = "Score";
	public String type = "dummy";
	public int target = 1;
	public float conversion = 1F;
	public String suffix = "";
	public ScoreOperation operation = ScoreOperation.MORE_OR_EQUAL;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskScoreboard.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.scoreboard";
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
	public void resetUser(UUID uuid)
	{
		completeUsers.remove(uuid);
	}

	@Override
	public void resetAll()
	{
		completeUsers.clear();
	}
	
	@Override
	public void update(EntityPlayer player, IQuest quest)
	{
		if(player.ticksExisted%20 == 0 && !QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)) // Auto-detect once per second
		{
			detect(player, quest);
		}
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		if(isComplete(player.getGameProfile().getId()))
		{
			return;
		}
		
		Scoreboard board = player.getWorldScoreboard();
		ScoreObjective scoreObj = board == null? null : board.getObjective(scoreName);
		
		if(scoreObj == null)
		{
			try
			{
		        IScoreObjectiveCriteria criteria = (IScoreObjectiveCriteria)IScoreObjectiveCriteria.field_96643_a.get(type);
		        criteria = criteria != null? criteria : new ScoreDummyCriteria(scoreName);
				scoreObj = board.addScoreObjective(scoreName, criteria);
				scoreObj.setDisplayName(scoreName);
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to create score '" + scoreName + "' for task!", e);
			}
		}
		
		Score score = board.func_96529_a(player.getCommandSenderName(), scoreObj);
		int points = score.getScorePoints();
		ScoreboardBQ.setScore(player, scoreName, points);
		
		if(operation.checkValues(points, target))
		{
			setComplete(player.getGameProfile().getId());
		}
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
		
		json.addProperty("scoreName", scoreName);
		json.addProperty("type", type);
		json.addProperty("target", target);
		json.addProperty("unitConversion", conversion);
		json.addProperty("unitSuffix", suffix);
		json.addProperty("operation", operation.name());
		
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
		
		scoreName = JsonHelper.GetString(json, "scoreName", "Score");
		scoreName.replaceAll(" ", "_");
		type = JsonHelper.GetString(json, "type", "dummy");
		target = JsonHelper.GetNumber(json, "target", 1).intValue();
		conversion = JsonHelper.GetNumber(json, "unitConversion", conversion).floatValue();
		suffix = JsonHelper.GetString(json, "unitSuffix", suffix);
		operation = ScoreOperation.valueOf(JsonHelper.GetString(json, "operation", "MORE_OR_EQUAL").toUpperCase());
		operation = operation != null? operation : ScoreOperation.MORE_OR_EQUAL;
	}

	private JsonObject writeProgressToJson(JsonObject json)
	{
		JsonArray jArray = new JsonArray();
		for(UUID uuid : completeUsers)
		{
			jArray.add(new JsonPrimitive(uuid.toString()));
		}
		json.add("completeUsers", jArray);
		
		return json;
	}

	private void readProgressFromJson(JsonObject json)
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
	}
	
	public static enum ScoreOperation
	{
		EQUAL("="),
		LESS_THAN("<"),
		MORE_THAN(">"),
		LESS_OR_EQUAL("<="),
		MORE_OR_EQUAL(">="),
		NOT("=/=");
		
		String text = "";
		ScoreOperation(String text)
		{
			this.text = text;
		}
		
		public String GetText()
		{
			return text;
		}
		
		public boolean checkValues(int n1, int n2)
		{
			switch(this)
			{
				case EQUAL:
					return n1 == n2;
				case LESS_THAN:
					return n1 < n2;
				case MORE_THAN:
					return n1 > n2;
				case LESS_OR_EQUAL:
					return n1 <= n2;
				case MORE_OR_EQUAL:
					return n1 >= n2;
				case NOT:
					return n1 != n2;
			}
			
			return false;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiEmbedded getTaskGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new GuiTaskScoreboard(this, posX, posY, sizeX, sizeY);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
	{
		return new GuiScoreEditor(parent, this);
	}
}
