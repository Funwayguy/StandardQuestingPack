package bq_standard.tasks;

import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
import bq_standard.client.gui.tasks.GuiTaskLocation;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskLocation;

public class TaskLocation implements ITask, ITickableTask
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
	public void updateTask(EntityPlayer player, IQuest quest)
	{
		if(player.ticksExisted%100 == 0 && !QuestingAPI.getAPI(ApiReference.SETTINGS).getProperty(NativeProps.EDIT_MODE)) // Only auto-detect every 5 seconds
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
	public NBTTagCompound writeToNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.PROGRESS)
		{
			return this.writeProgressToJson(json);
		} else if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		json.setString("name", name);
		json.setInteger("posX", x);
		json.setInteger("posY", y);
		json.setInteger("posZ", z);
		json.setInteger("dimension", dim);
		json.setInteger("range", range);
		json.setBoolean("visible", visible);
		json.setBoolean("hideInfo", hideInfo);
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.PROGRESS)
		{
			this.readProgressFromJson(json);
			return;
		} else if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		name = json.getString("name");
		x = json.getInteger("posX");
		y = json.getInteger("posY");
		z = json.getInteger("posZ");
		dim = json.getInteger("dimension");
		range = json.getInteger("range");
		visible = json.getBoolean("visible");
		hideInfo = json.getBoolean("hideInfo");
	}

	private NBTTagCompound writeProgressToJson(NBTTagCompound json)
	{
		NBTTagList jArray = new NBTTagList();
		for(UUID uuid : completeUsers)
		{
			jArray.appendTag(new NBTTagString(uuid.toString()));
		}
		json.setTag("completeUsers", jArray);
		
		return json;
	}

	private void readProgressFromJson(NBTTagCompound json)
	{
		completeUsers = new ArrayList<UUID>();
		NBTTagList cList = json.getTagList("completeUsers", 8);
		for(int i = 0; i < cList.tagCount(); i++)
		{
			NBTBase entry = cList.get(i);
			
			if(entry == null || entry.getId() != 8)
			{
				continue;
			}
			
			try
			{
				completeUsers.add(UUID.fromString(((NBTTagString)entry).getString()));
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

	@Override
	public IJsonDoc getDocumentation()
	{
		return null;
	}
}
