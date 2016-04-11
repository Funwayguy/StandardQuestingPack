package bq_standard.tasks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.JsonHelper;
import bq_standard.client.gui.tasks.GuiTaskLocation;
import com.google.gson.JsonObject;

public class TaskLocation extends TaskBase
{
	public String name = "New Location";
	public int x = 0;
	public int y = 0;
	public int z = 0;
	public int dim = 0;
	public int range = -1;
	public boolean visible = false;
	public boolean hideInfo = false;
	
	@Override
	public String getUnlocalisedName()
	{
		return "bq_standard.task.location";
	}
	
	@Override
	public void Update(EntityPlayer player)
	{
		if(player.ticksExisted%100 != 0 && !QuestDatabase.editMode) // Only auto-detect every 5 seconds
		{
			return;
		}
		
		Detect(player);
	}
	
	@Override
	public void Detect(EntityPlayer player)
	{
		if(isComplete(player.getUniqueID()))
		{
			return; // Keeps ray casting calls to a minimum
		}
		
		if(player.dimension == dim && (range <= 0 || player.getDistance(x, y, z) <= range))
		{
			if(visible && range > 0) // Do not do ray casting with infinite range!
			{
				Vec3d pPos = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
				Vec3d tPos = new Vec3d(x, y, z);
				boolean liquids = false;
				RayTraceResult mop = player.worldObj.rayTraceBlocks(pPos, tPos, liquids, !liquids, false);
				
				if(mop == null || mop.typeOfHit != RayTraceResult.Type.BLOCK)
				{
					setCompletion(player.getUniqueID(), true);
				} else
				{
					return;
				}
			} else
			{
				setCompletion(player.getUniqueID(), true);
			}
		}
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("name", name);
		json.addProperty("posX", x);
		json.addProperty("posY", y);
		json.addProperty("posZ", z);
		json.addProperty("dimension", dim);
		json.addProperty("range", range);
		json.addProperty("visible", visible);
		json.addProperty("hideInfo", hideInfo);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		super.readFromJson(json);
		
		name = JsonHelper.GetString(json, "name", "New Location");
		x = JsonHelper.GetNumber(json, "posX", 0).intValue();
		y = JsonHelper.GetNumber(json, "posY", 0).intValue();
		z = JsonHelper.GetNumber(json, "posZ", 0).intValue();
		dim = JsonHelper.GetNumber(json, "dimension", 0).intValue();
		range = JsonHelper.GetNumber(json, "range", -1).intValue();
		visible = JsonHelper.GetBoolean(json, "visible", false);
		hideInfo = JsonHelper.GetBoolean(json, "hideInfo", false);
	}

	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskLocation(this, screen, posX, posY, sizeX, sizeY);
	}
}
