package bq_standard.tasks;

import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.ItemComparison;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.NBTConverter;
import bq_standard.client.gui.editors.GuiMeetingEditor;
import bq_standard.client.gui.tasks.GuiTaskMeeting;
import com.google.gson.JsonObject;

public class TaskMeeting extends TaskBase
{
	public String idName = "Villager";
	public int range = 4;
	public int amount = 1;
	public boolean ignoreNBT = true;
	public boolean subtypes = true;
	
	/**
	 * NBT representation of the intended target. Used only for NBT comparison checks
	 */
	public NBTTagCompound targetTags;
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.meeting";
	}
	
	@Override
	public void Update(QuestInstance quest, EntityPlayer player)
	{
		if(player.ticksExisted%60 == 0 && !QuestDatabase.editMode)
		{
			Detect(quest, player);
		}
	}
	
	@Override
	public void Detect(QuestInstance quest, EntityPlayer player)
	{
		if(!player.isEntityAlive() || isComplete(player.getUniqueID()))
		{
			return;
		}
		
		List<Entity> list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(range, range, range));
		Class<? extends Entity> target = (Class<? extends Entity>)EntityList.stringToClassMapping.get(idName);
		
		if(target == null)
		{
			return;
		}
		
		int n = 0;
		
		for(Entity entity : list)
		{
			Class<? extends Entity> subject = entity.getClass();
			
			if(subtypes && !target.isAssignableFrom(subject))
			{
				continue; // This is not the intended target or sub-type
			} else if(!subtypes && !EntityList.getEntityString(entity).equals(idName))
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
				setCompletion(player.getUniqueID(), true);
				return;
			}
		}
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("target", idName);
		json.addProperty("range", range);
		json.addProperty("amount", amount);
		json.addProperty("subtypes", subtypes);
		json.addProperty("ignoreNBT", ignoreNBT);
		json.add("targetNBT", NBTConverter.NBTtoJSON_Compound(targetTags, new JsonObject()));
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		super.writeToJson(json);
		
		idName = JsonHelper.GetString(json, "target", "Villager");
		range = JsonHelper.GetNumber(json, "range", 4).intValue();
		amount = JsonHelper.GetNumber(json, "amount", 1).intValue();
		subtypes = JsonHelper.GetBoolean(json, "subtypes", true);
		ignoreNBT = JsonHelper.GetBoolean(json, "ignoreNBT", true);
		targetTags = NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(json, "targetNBT"), new NBTTagCompound());
	}
	
	/**
	 * Returns a new editor screen for this Reward type to edit the given data
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen GetEditor(GuiScreen parent, JsonObject data)
	{
		return new GuiMeetingEditor(parent, data);
	}

	@Override
	public GuiEmbedded getGui(QuestInstance quest, GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskMeeting(this, screen, posX, posY, sizeX, sizeY);
	}
}
