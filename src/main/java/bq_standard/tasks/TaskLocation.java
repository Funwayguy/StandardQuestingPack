package bq_standard.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import bq_standard.client.gui.tasks.PanelTaskLocation;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskLocation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TaskLocation implements ITaskTickable
{
	private ArrayList<UUID> completeUsers = new ArrayList<>();
	public String name = "New Location";
	public int x = 0;
	public int y = 0;
	public int z = 0;
	public int dim = 0;
	public int range = -1;
	public boolean visible = false;
	public boolean hideInfo = false;
	public boolean invertDistance = false;
	public boolean taxiCab = false;
	
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
	public void tickTask(@Nonnull DBEntry<IQuest> quest, @Nonnull EntityPlayer player)
	{
		if(player.ticksExisted%100 == 0) // Only auto-detect every 5 seconds
		{
			detect(player, quest.getValue());
		}
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		UUID playerID = QuestingAPI.getQuestingUUID(player);
		
		if(!player.isEntityAlive() || isComplete(playerID)) return;
		
		if(player.dimension == dim && (range <= 0 || (getDistance(player) <= range) != invertDistance))
		{
			if(visible && range > 0) // Do not do ray casting with infinite range!
			{
				Vec3 pPos = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
				Vec3 tPos = Vec3.createVectorHelper(x, y, z);
				MovingObjectPosition mop = player.worldObj.func_147447_a(pPos, tPos, false, true, false);
				
				if(mop == null || mop.typeOfHit != MovingObjectType.BLOCK)
				{
					setComplete(playerID);
				}
			} else
			{
				setComplete(playerID);
			}
		}
	}
	
	private double getDistance(EntityPlayer player)
    {
        if(!taxiCab)
        {
            return player.getDistance(x, y, z);
        } else
        {
            return Math.abs(player.posX - x) + Math.abs(player.posY - y) + Math.abs(player.posZ - z);
        }
    }
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json)
	{
		json.setString("name", name);
		json.setInteger("posX", x);
		json.setInteger("posY", y);
		json.setInteger("posZ", z);
		json.setInteger("dimension", dim);
		json.setInteger("range", range);
		json.setBoolean("visible", visible);
		json.setBoolean("hideInfo", hideInfo);
		json.setBoolean("invertDistance", invertDistance);
		json.setBoolean("taxiCabDist", taxiCab);
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json)
	{
		name = json.getString("name");
		x = json.getInteger("posX");
		y = json.getInteger("posY");
		z = json.getInteger("posZ");
		dim = json.getInteger("dimension");
		range = json.getInteger("range");
		visible = json.getBoolean("visible");
		hideInfo = json.getBoolean("hideInfo");
		invertDistance = json.getBoolean("invertDistance");
		taxiCab = json.getBoolean("taxiCabDist");
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
		completeUsers = new ArrayList<>();
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

	@Override
	public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
	{
	    return new PanelTaskLocation(rect, quest, this);
	}

	@Override
	public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
	{
		return null;
	}
    
    private static final HashMap<Integer,String> dimNameCache = new HashMap<>();
	
    public static String getDimName(int dim)
	{
	    if(dimNameCache.containsKey(dim))
        {
            return dimNameCache.get(dim);
        }
        
	    try
        {
            WorldProvider prov = DimensionManager.createProviderFor(dim);
            if(prov != null)
            {
                dimNameCache.put(dim, prov.getDimensionName());
                return prov.getDimensionName();
            } else
            {
                dimNameCache.put(dim, "" + dim);
                return "" + dim;
            }
        } catch(Exception e)
        {
            dimNameCache.put(dim, "" + dim);
            return "" + dim;
        }
	}
}
