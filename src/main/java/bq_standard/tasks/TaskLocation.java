package bq_standard.tasks;

import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.apache.logging.log4j.Level;
import betterquesting.api.ExpansionAPI;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.properties.NativeProps;
import betterquesting.api.quests.tasks.ITask;
import betterquesting.api.utils.JsonHelper;
import betterquesting.quests.QuestSettings;
import bq_standard.client.gui.tasks.GuiTaskLocation;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskLocation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class TaskLocation implements ITask
{
	private ArrayList<UUID> completeUsers = new ArrayList<UUID>();
	public String name = "New Location";
	public int x = 0;
	public int y = 0;
	public int z = 0;
	public int dim = 0;
	public int range = -1;
	public boolean visible = false;
	public boolean hideInfo = false;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskLocation.INSTANCE.getRegistryName();
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.location";
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
		if(player.ticksExisted%100 == 0 && !QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)) // Only auto-detect every 5 seconds
		{
			detect(player, quest);
		}
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		UUID playerID = ExpansionAPI.getAPI().getNameCache().getQuestingID(player);
		
		if(!player.isEntityAlive() || isComplete(playerID))
		{
			return; // Keeps ray casting calls to a minimum
		}
		
		if(player.dimension == dim && (range <= 0 || player.getDistance(x, y, z) <= range))
		{
			if(visible && range > 0) // Do not do ray casting with infinite range!
			{
				Vec3 pPos = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
				Vec3 tPos = Vec3.createVectorHelper(x, y, z);
				boolean liquids = false;
				MovingObjectPosition mop = player.worldObj.func_147447_a(pPos, tPos, liquids, !liquids, false);
				
				if(mop == null || mop.typeOfHit != MovingObjectType.BLOCK)
				{
					setComplete(playerID);
				} else
				{
					return;
				}
			} else
			{
				setComplete(playerID);
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
		
		json.addProperty("name", name);
		json.addProperty("posX", x);
		json.addProperty("posY", y);
		json.addProperty("posZ", z);
		json.addProperty("dimension", dim);
		json.addProperty("range", range);
		json.addProperty("visible", visible);
		json.addProperty("hideInfo", hideInfo);
		
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
		
		name = JsonHelper.GetString(json, "name", "New Location");
		x = JsonHelper.GetNumber(json, "posX", 0).intValue();
		y = JsonHelper.GetNumber(json, "posY", 0).intValue();
		z = JsonHelper.GetNumber(json, "posZ", 0).intValue();
		dim = JsonHelper.GetNumber(json, "dimension", 0).intValue();
		range = JsonHelper.GetNumber(json, "range", -1).intValue();
		visible = JsonHelper.GetBoolean(json, "visible", false);
		hideInfo = JsonHelper.GetBoolean(json, "hideInfo", false);
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

	@Override
	public IGuiEmbedded getTaskGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new GuiTaskLocation(this, posX, posY, sizeX, sizeY);
	}

	@Override
	public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
	{
		return null;
	}
}
