package bq_standard.rewards;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.utils.JsonHelper;
import bq_standard.client.gui.rewards.GuiRewardScoreboard;
import bq_standard.core.BQ_Standard;
import bq_standard.rewards.factory.FactoryRewardScoreboard;
import com.google.gson.JsonObject;

public class RewardScoreboard implements IReward
{
	public String score = "Reputation";
	public String type = "dummy";
	public boolean relative = true;
	public int value = 1;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryRewardScoreboard.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.reward.scoreboard";
	}
	
	@Override
	public boolean canClaim(EntityPlayer player, IQuest quest)
	{
		return true;
	}
	
	@Override
	public void claimReward(EntityPlayer player, IQuest quest)
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
	public void readFromJson(JsonObject json, EnumSaveType saveType)
	{
		score = JsonHelper.GetString(json, "score", "Reputation");
		type = JsonHelper.GetString(json, "type", "dummy");
		value = JsonHelper.GetNumber(json, "value", 1).intValue();
		relative = JsonHelper.GetBoolean(json, "relative", true);
	}
	
	@Override
	public JsonObject writeToJson(JsonObject json, EnumSaveType saveType)
	{
		json.addProperty("score", score);
		json.addProperty("type", "dummy");
		json.addProperty("value", value);
		json.addProperty("relative", relative);
		return json;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiEmbedded getRewardGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new GuiRewardScoreboard(this, posX, posY, sizeX, sizeY);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getRewardEditor(GuiScreen screen, IQuest quest)
	{
		return null;
	}

	@Override
	public IJsonDoc getDocumentation()
	{
		return null;
	}
}
