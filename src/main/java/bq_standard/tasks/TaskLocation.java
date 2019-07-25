package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import bq_standard.client.gui.tasks.PanelTaskLocation;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskLocation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
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
	public String structure = "";
	public int biome = -1;
	public int x = 0;
	public int y = 0;
	public int z = 0;
	public int dim = 0;
	public int range = -1;
	public boolean visible = false;
	public boolean hideInfo = false;
	public boolean invert = false;
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
		
		if(!player.isEntityAlive() || isComplete(playerID) || !(player instanceof EntityPlayerMP)) return;
		
		EntityPlayerMP playerMP = (EntityPlayerMP)player;
		QuestCache qc = (QuestCache)player.getExtendedProperties(QuestCache.LOC_QUEST_CACHE.toString());
		
		boolean flag = false;
		
		if(player.dimension == dim && (range <= 0 || getDistance(player) <= range))
		{
			if(biome >= 0 && biome != playerMP.getServerForPlayer().getBiomeGenForCoords(playerMP.serverPosX, playerMP.serverPosZ).biomeID)
            {
                if(!invert) return;
            } else if(!StringUtils.isNullOrEmpty(structure) && playerMP.getServerForPlayer().getChunkProvider().func_147416_a(playerMP.getServerForPlayer(), structure, playerMP.serverPosX, playerMP.serverPosY, playerMP.serverPosZ) == null)
            {
                if(!invert) return;
            } else if(visible && range > 0) // Do not do ray casting with infinite range!
			{
				Vec3 pPos = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
				Vec3 tPos = Vec3.createVectorHelper(x, y, z);
				MovingObjectPosition mop = player.worldObj.func_147447_a(pPos, tPos, false, true, false);
				
				flag = mop == null || mop.typeOfHit != MovingObjectType.BLOCK;
			} else
			{
				flag = true;
			}
		}
		
		if(flag != invert)
        {
            setComplete(playerID);
            if(qc != null) qc.markQuestDirty(QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
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
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		nbt.setString("name", name);
		nbt.setInteger("posX", x);
		nbt.setInteger("posY", y);
		nbt.setInteger("posZ", z);
		nbt.setInteger("dimension", dim);
		nbt.setInteger("range", range);
		nbt.setBoolean("visible", visible);
		nbt.setBoolean("hideInfo", hideInfo);
		nbt.setBoolean("invert", invert);
		nbt.setBoolean("taxiCabDist", taxiCab);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		name = nbt.getString("name");
		x = nbt.getInteger("posX");
		y = nbt.getInteger("posY");
		z = nbt.getInteger("posZ");
		dim = nbt.getInteger("dimension");
		range = nbt.getInteger("range");
		visible = nbt.getBoolean("visible");
		hideInfo = nbt.getBoolean("hideInfo");
		invert = nbt.getBoolean("invert") || nbt.getBoolean("invertDistance");
		taxiCab = nbt.getBoolean("taxiCabDist");
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
