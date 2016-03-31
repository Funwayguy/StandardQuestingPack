package bq_standard.rewards;

import org.apache.logging.log4j.Level;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreDummyCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.rewards.RewardBase;
import betterquesting.utils.JsonHelper;
import bq_standard.client.gui.rewards.GuiRewardScoreboard;
import bq_standard.core.BQ_Standard;
import com.google.gson.JsonObject;

public class RewardScoreboard extends RewardBase
{
	public String score = "Reputation";
	public String type = "dummy";
	public boolean relative = true;
	public int value = 1;
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.reward.scoreboard";
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, NBTTagCompound choiceData)
	{
		return true;
	}
	
	@Override
	public void Claim(EntityPlayer player, NBTTagCompound choiceData)
	{
		Scoreboard board = player.getWorldScoreboard();
		
		if(board == null)
		{
			return;
		}
		
		ScoreObjective scoreObj = board.getObjective(score);
		
		if(scoreObj == null)
		{
			try
			{
		        IScoreObjectiveCriteria criteria = (IScoreObjectiveCriteria)IScoreObjectiveCriteria.field_96643_a.get(type);
		        criteria = criteria != null? criteria : new ScoreDummyCriteria(score);
				scoreObj = board.addScoreObjective(score, criteria);
				scoreObj.setDisplayName(score);
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to create score '" + score + "' for reward!", e);
			}
		}
		
		if(scoreObj.getCriteria().isReadOnly())
		{
			return;
		}
		
		Score s = board.func_96529_a(player.getCommandSenderName(), scoreObj);
		
		if(relative)
		{
			s.increseScore(value);
		} else
		{
			s.setScorePoints(value);
		}
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		score = JsonHelper.GetString(json, "score", "Reputation");
		type = JsonHelper.GetString(json, "type", "dummy");
		value = JsonHelper.GetNumber(json, "value", 1).intValue();
		relative = JsonHelper.GetBoolean(json, "relative", true);
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		json.addProperty("score", score);
		json.addProperty("type", "dummy");
		json.addProperty("value", value);
		json.addProperty("relative", relative);
	}

	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiRewardScoreboard(this, screen, posX, posY, sizeX, sizeY);
	}
}
