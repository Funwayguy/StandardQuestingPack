package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.party.IParty;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import bq_standard.ScoreboardBQ;
import bq_standard.client.gui.editors.tasks.GuiEditTaskScoreboard;
import bq_standard.client.gui.tasks.PanelTaskScoreboard;
import bq_standard.core.BQ_Standard;
import bq_standard.network.handlers.NetScoreSync;
import bq_standard.tasks.factory.FactoryTaskScoreboard;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.scoreboard.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class TaskScoreboard implements ITaskTickable
{
	private final Set<UUID> completeUsers = new TreeSet<>();
	public String scoreName = "Score";
	public String scoreDisp = "Score";
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
		completeUsers.add(uuid);
	}

	@Override
	public void resetUser(@Nullable UUID uuid)
	{
	    if(uuid == null)
        {
		    completeUsers.clear();
        } else
        {
            completeUsers.remove(uuid);
        }
	}
	
	@Override
	public void tickTask(@Nonnull EntityPlayer player, Runnable callback)
	{
		if(player.ticksExisted%20 == 0 && internalDetect(player) && callback != null) callback.run(); // Auto-detect once per second
	}
	
	@Override
	public void detect(@Nonnull EntityPlayer player, IQuest quest)
	{
		if(internalDetect(player))
		{
		    QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
			if(qc != null) qc.markQuestDirty(QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
		}
	}
	
	private boolean internalDetect(@Nonnull EntityPlayer player)
    {
	    UUID playerID = QuestingAPI.getQuestingUUID(player);
     
		Scoreboard board = player.getWorldScoreboard();
		ScoreObjective scoreObj = board.getObjective(scoreName);
		
		if(scoreObj == null)
		{
			try
			{
		        IScoreCriteria criteria = IScoreCriteria.INSTANCES.get(type);
		        criteria = criteria != null? criteria : new ScoreCriteria(scoreName);
				scoreObj = board.addScoreObjective(scoreName, criteria);
				scoreObj.setDisplayName(scoreDisp);
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to create score '" + scoreName + "' for task!", e);
				return false;
			}
		}

		Score score = board.getOrCreateScore(player.getName(), scoreObj);
		int points = score.getScorePoints();
		ScoreboardBQ.INSTANCE.setScore(playerID, scoreName, points);
		
		if(player instanceof EntityPlayerMP)
        {
            NetScoreSync.sendScore((EntityPlayerMP)player);
        }
		
		if(operation.checkValues(points, target))
		{
            DBEntry<IParty> party = QuestingAPI.getAPI(ApiReference.PARTY_DB).getParty(playerID);
            final List<UUID> progress = party == null ? Collections.singletonList(playerID) : party.getValue().getMembers();
            progress.forEach((value) -> {
                if(isComplete(value)) return;
                setComplete(value);
            });
			return true;
		}
		
		return false;
    }
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json)
	{
		json.setString("scoreName", scoreName);
		json.setString("scoreDisp", scoreDisp);
		json.setString("type", type);
		json.setInteger("target", target);
		json.setFloat("unitConversion", conversion);
		json.setString("unitSuffix", suffix);
		json.setString("operation", operation.name());
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json)
	{
		scoreName = json.getString("scoreName");
		scoreName = scoreName.replaceAll(" ", "_");
		scoreDisp = json.getString("scoreDisp");
		type = json.hasKey("type", 8) ? json.getString("type") : "dummy";
		target = json.getInteger("target");
		conversion = json.getFloat("unitConversion");
		suffix = json.getString("unitSuffix");
		try
        {
            operation = ScoreOperation.valueOf(json.hasKey("operation", 8) ? json.getString("operation") : "MORE_OR_EQUAL");
        } catch(Exception e)
        {
            operation = ScoreOperation.MORE_OR_EQUAL;
        }
	}
	
	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, @Nullable List<UUID> users)
	{
		NBTTagList jArray = new NBTTagList();
		
		if(users != null)
        {
            users.forEach((uuid) -> {
                if(completeUsers.contains(uuid)) jArray.appendTag(new NBTTagString(uuid.toString()));
            });
        } else
        {
            completeUsers.forEach((uuid) -> jArray.appendTag(new NBTTagString(uuid.toString())));
        }
		
		nbt.setTag("completeUsers", jArray);
		
		return nbt;
	}
 
	@Override
	public void readProgressFromNBT(NBTTagCompound nbt, boolean merge)
	{
		if(!merge) completeUsers.clear();
		NBTTagList cList = nbt.getTagList("completeUsers", 8);
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
	}
	
	public enum ScoreOperation
	{
		EQUAL("="),
		LESS_THAN("<"),
		MORE_THAN(">"),
		LESS_OR_EQUAL("<="),
		MORE_OR_EQUAL(">="),
		NOT("=/=");
		
		private final String text;
		
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
	public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
	{
	    return new PanelTaskScoreboard(rect, this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
	{
	    return new GuiEditTaskScoreboard(parent, quest, this);
	}
}
