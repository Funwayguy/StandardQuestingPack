package bq_standard.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
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
	public void Update(EntityPlayer player)
	{
		if(player == null || player.worldObj.isRemote || player.ticksExisted%20 != 0)
		{
			return;
		} else
		{
			this.Detect(player);
		}
	}
	
	@Override
	public void Detect(EntityPlayer player)
	{
		if(player == null || player.worldObj.isRemote)
		{
			return;
		}
		
		List<Entity> list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(4D, 4D, 4D));
		Class<? extends Entity> target = (Class<? extends Entity>)EntityList.stringToClassMapping.get(idName);
		
		if(target == null)
		{
			return;
		}
		
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
			
			this.completeUsers.add(player.getUniqueID());
			return;
		}
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("target", idName);
		json.addProperty("subtypes", subtypes);
		json.addProperty("ignoreNBT", ignoreNBT);
		json.add("targetNBT", NBTConverter.NBTtoJSON_Compound(targetTags, new JsonObject()));
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		super.writeToJson(json);
		
		idName = JsonHelper.GetString(json, "target", "Villager");
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
	public void ResetProgress(UUID uuid)
	{
		completeUsers.remove(uuid);
	}

	@Override
	public void ResetAllProgress()
	{
		completeUsers = new ArrayList<UUID>();
	}

	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskMeeting(this, screen, posX, posY, sizeX, sizeY);
	}
}
