package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import bq_standard.client.gui.editors.tasks.GuiEditTaskMeeting;
import bq_standard.client.gui.tasks.PanelTaskMeeting;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskMeeting;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskMeeting implements ITaskTickable
{
	private final List<UUID> completeUsers = new ArrayList<>();
	
	public String idName = "minecraft:villager";
	public int range = 4;
	public int amount = 1;
	public boolean ignoreNBT = true;
	public boolean subtypes = true;
	
	/**
	 * NBT representation of the intended target. Used only for NBT comparison checks
	 */
	public NBTTagCompound targetTags = new NBTTagCompound();
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskMeeting.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.meeting";
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
	public void tickTask(@Nonnull DBEntry<IQuest> quest, @Nonnull EntityPlayer player)
	{
		if(player.ticksExisted%60 == 0)
		{
			detect(player, quest.getValue());
		}
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		if(!player.isEntityAlive() || isComplete(playerID)) return;
		
		List<Entity> list = player.world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(range, range, range));
		ResourceLocation targetID = new ResourceLocation(idName);
		Class<? extends Entity> target = EntityList.getClass(targetID);
		
		if(target == null) return;
		
		int n = 0;
		
		for(Entity entity : list)
		{
			Class<? extends Entity> subject = entity.getClass();
			ResourceLocation subjectID = EntityList.getKey(subject);
			
			if(subjectID == null)
			{
				continue;
			} else if(subtypes && !target.isAssignableFrom(subject))
			{
				continue; // This is not the intended target or sub-type
			} else if(!subtypes && !subjectID.equals(targetID))
			{
				continue; // This isn't the exact target required
			}
			
			NBTTagCompound subjectTags = new NBTTagCompound();
			entity.writeToNBTOptional(subjectTags);
			if(!ignoreNBT && !ItemComparison.CompareNBTTag(targetTags, subjectTags, true))
			{
				continue;
			}
			
			n++;
			
			if(n >= amount)
			{
				setComplete(playerID);
                QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
                if(qc != null) qc.markQuestDirty(QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
				return;
			}
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json)
	{
		json.setString("target", idName);
		json.setInteger("range", range);
		json.setInteger("amount", amount);
		json.setBoolean("subtypes", subtypes);
		json.setBoolean("ignoreNBT", ignoreNBT);
		json.setTag("targetNBT", targetTags);
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json)
	{
		idName = json.hasKey("target", 8) ? json.getString("target") : "minecraft:villager";
		range = json.getInteger("range");
		amount = json.getInteger("amount");
		subtypes = json.getBoolean("subtypes");
		ignoreNBT = json.getBoolean("ignoreNBT");
		targetTags = json.getCompoundTag("targetNBT");
	}

	@Override
	public NBTTagCompound writeProgressToNBT(NBTTagCompound json, List<UUID> users)
	{
		NBTTagList jArray = new NBTTagList();
		for(UUID uuid : completeUsers)
		{
			jArray.appendTag(new NBTTagString(uuid.toString()));
		}
		json.setTag("completeUsers", jArray);
		
		return json;
	}
	
	@Override
	public void readProgressFromNBT(NBTTagCompound json, boolean merge)
	{
		completeUsers.clear();
		NBTTagList cList = json.getTagList("completeUsers", 8);
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
	
	/**
	 * Returns a new editor screen for this Reward type to edit the given data
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
	{
	    return new GuiEditTaskMeeting(parent, quest, this);
	}

	@Override
	public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
	{
	    return new PanelTaskMeeting(rect, quest, this);
	}
}
