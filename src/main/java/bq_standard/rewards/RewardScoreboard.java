package bq_standard.rewards;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import org.apache.logging.log4j.Level;
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
		        IScoreCriteria criteria = IScoreCriteria.INSTANCES.get(type);
		        criteria = criteria != null? criteria : new ScoreCriteria(score);
				scoreObj = board.addScoreObjective(score, criteria);
				scoreObj.setDisplayName(score);
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to create score '" + score + "' for reward!", e);
			}
		}
		
		if(scoreObj == null || scoreObj.getCriteria().isReadOnly())
		{
			return;
		}
		
		Score s = board.getOrCreateScore(player.getName(), scoreObj);
		
		if(relative)
		{
			s.increaseScore(value);
		} else
		{
			s.setScorePoints(value);
		}
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		score = JsonHelper.GetString(json, "score", "Reputation");
		type = JsonHelper.GetString(json, "type", type);
		value = JsonHelper.GetNumber(json, "value", 1).intValue();
		relative = JsonHelper.GetBoolean(json, "relative", true);
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		json.addProperty("score", score);
		json.addProperty("type", type);
		json.addProperty("value", value);
		json.addProperty("relative", relative);
	}

	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiRewardScoreboard(this, screen, posX, posY, sizeX, sizeY);
	}
}
