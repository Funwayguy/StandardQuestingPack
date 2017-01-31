package bq_standard.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.questing.tasks.ITickableTask;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import bq_standard.client.gui.editors.GuiMeetingEditor;
import bq_standard.client.gui.tasks.GuiTaskMeeting;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskMeeting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class TaskMeeting implements ITask, ITickableTask
{
	private ArrayList<UUID> completeUsers = new ArrayList<UUID>();
	
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
	@Deprecated
	public void update(EntityPlayer player, IQuest quest){}
	
	@Override
	public void updateTask(EntityPlayer player, IQuest quest)
	{
		if(player.ticksExisted%60 == 0 && !QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE))
		{
			detect(player, quest);
		}
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		if(!player.isEntityAlive() || isComplete(playerID))
		{
			return;
		}
		
		List<Entity> list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(range, range, range));
		Class<? extends Entity> target = (Class<? extends Entity>)EntityList.NAME_TO_CLASS.get(idName);
		
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
				setComplete(playerID);
				return;
			}
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
		
		json.addProperty("target", idName);
		json.addProperty("range", range);
		json.addProperty("amount", amount);
		json.addProperty("subtypes", subtypes);
		json.addProperty("ignoreNBT", ignoreNBT);
		json.add("targetNBT", NBTConverter.NBTtoJSON_Compound(targetTags, new JsonObject(), true));
		
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
		
		idName = JsonHelper.GetString(json, "target", "Villager");
		range = JsonHelper.GetNumber(json, "range", 4).intValue();
		amount = JsonHelper.GetNumber(json, "amount", 1).intValue();
		subtypes = JsonHelper.GetBoolean(json, "subtypes", true);
		ignoreNBT = JsonHelper.GetBoolean(json, "ignoreNBT", true);
		targetTags = NBTConverter.JSONtoNBT_Object(JsonHelper.GetObject(json, "targetNBT"), new NBTTagCompound(), true);
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
	
	/**
	 * Returns a new editor screen for this Reward type to edit the given data
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
	{
		return new GuiMeetingEditor(parent, this);
	}

	@Override
	public IGuiEmbedded getTaskGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new GuiTaskMeeting(this, posX, posY, sizeX, sizeY);
	}

	@Override
	public IJsonDoc getDocumentation()
	{
		return null;
	}
}
