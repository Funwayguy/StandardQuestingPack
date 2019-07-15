package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.storage.DBEntry;
import bq_standard.client.gui.tasks.PanelTaskInteractEntity;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskInteractEntity;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class TaskInteractEntity implements ITask
{
	private final List<UUID> completeUsers = new ArrayList<>();
	private final HashMap<UUID, Integer> userProgress = new HashMap<>();
	
    public BigItemStack targetItem = new BigItemStack(Items.AIR);
	public boolean ignoreItemNBT = false;
	public boolean partialItemMatch = true;
	public boolean useMainHand = true;
	public boolean useOffHand = true;
    
    public String entityID = "minecraft:villager";
    public NBTTagCompound entityTags = new NBTTagCompound();
    public boolean entitySubtypes = true;
	public boolean ignoreEntityNBT = true;
	
	public boolean onInteract = true;
	public boolean onHit = false;
	public int required = 1;
    
    @Override
    public String getUnlocalisedName()
    {
        return BQ_Standard.MODID + ".task.interact_entity";
    }
    
    @Override
    public ResourceLocation getFactoryID()
    {
        return FactoryTaskInteractEntity.INSTANCE.getRegistryName();
    }
    
    public void onInteract(DBEntry<IQuest> quest, EntityPlayer player, EnumHand hand, ItemStack item, Entity entity, boolean isHit)
    {
        UUID playerID = QuestingAPI.getQuestingUUID(player);
        if(isComplete(playerID)) return;
        
        if((!onHit && isHit) || (!onInteract && !isHit)) return;
        if((!useMainHand && hand == EnumHand.MAIN_HAND) || (!useOffHand && hand == EnumHand.OFF_HAND)) return;
        
        ResourceLocation targetRes = new ResourceLocation(entityID);
        Class<? extends Entity> targetClass = EntityList.getClass(targetRes);
        if(targetClass == null) return; // No idea what we're looking for
        
        Class<? extends Entity> subjectClass = entity.getClass();
        ResourceLocation subjectRes = EntityList.getKey(entity);
        if(subjectRes == null) return; // This isn't a registered entity!
        
        if(entitySubtypes ? !targetClass.isAssignableFrom(subjectClass) : !subjectRes.equals(targetRes)) return;
        
        if(!ignoreEntityNBT)
        {
            NBTTagCompound subjectTags = new NBTTagCompound();
            entity.writeToNBTOptional(subjectTags);
            if(!ItemComparison.CompareNBTTag(entityTags, subjectTags, true)) return;
        }
        
        if(targetItem.getBaseStack().getItem() != Items.AIR)
        {
            if(targetItem.hasOreDict() && !ItemComparison.OreDictionaryMatch(targetItem.getOreIngredient(), targetItem.GetTagCompound(), item, !ignoreItemNBT, partialItemMatch))
            {
                return;
            } else if(!ItemComparison.StackMatch(targetItem.getBaseStack(), item, !ignoreItemNBT, partialItemMatch))
            {
                return;
            }
        }
        
        int progress = getUsersProgress(playerID);
        setUserProgress(playerID, ++progress);
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        if(qc != null) qc.markQuestDirty(quest.getID());
        
        detect(player, quest.getValue());
    }
    
    @Override
    public void detect(EntityPlayer player, IQuest quest)
    {
        UUID playerID = QuestingAPI.getQuestingUUID(player);
        if(isComplete(playerID)) return;
        
        if(getUsersProgress(playerID) >= required)
        {
            setComplete(playerID);
            QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
            if(qc != null) qc.markQuestDirty(QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
        }
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
	public void resetUser(@Nullable UUID uuid)
	{
	    if(uuid == null)
        {
            completeUsers.clear();
            userProgress.clear();
        } else
        {
            completeUsers.remove(uuid);
            userProgress.remove(uuid);
        }
	}
    
    @Override
	@SideOnly(Side.CLIENT)
    public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
    {
        return new PanelTaskInteractEntity(rect, this);
    }
    
    @Override
    @Nullable
	@SideOnly(Side.CLIENT)
    public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
    {
        return null;
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
		
		userProgress.clear();
		NBTTagList pList = json.getTagList("userProgress", 10);
		for(int i = 0; i < pList.tagCount(); i++)
		{
			NBTTagCompound pTag = pList.getCompoundTagAt(i);
			UUID uuid;
			try
			{
				uuid = UUID.fromString(pTag.getString("uuid"));
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load user progress for task", e);
				continue;
			}
			
			userProgress.put(uuid, pTag.getInteger("value"));
		}
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
		
		NBTTagList progArray = new NBTTagList();
		for(Entry<UUID,Integer> entry : userProgress.entrySet())
		{
			NBTTagCompound pJson = new NBTTagCompound();
			pJson.setString("uuid", entry.getKey().toString());
			pJson.setInteger("value", entry.getValue());
			progArray.appendTag(pJson);
		}
		json.setTag("userProgress", progArray);
		
		return json;
	}
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setTag("item", targetItem .writeToNBT(new NBTTagCompound()));
        nbt.setBoolean("ignoreItemNBT", ignoreItemNBT);
        nbt.setBoolean("partialItemMatch", partialItemMatch);
        
        nbt.setString("targetID", entityID);
        nbt.setTag("targetNBT", entityTags);
        nbt.setBoolean("ignoreTargetNBT", ignoreEntityNBT);
        nbt.setBoolean("targetSubtypes", entitySubtypes);
        
        nbt.setBoolean("allowMainHand", useMainHand);
        nbt.setBoolean("allowOffHand", useOffHand);
        nbt.setInteger("requiredUses", required);
        nbt.setBoolean("onInteract", onInteract);
        nbt.setBoolean("onHit", onHit);
        return nbt;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        targetItem = new BigItemStack(nbt.getCompoundTag("item"));
        ignoreItemNBT = nbt.getBoolean("ignoreItemNBT");
        partialItemMatch = nbt.getBoolean("partialItemMatch");
        
        entityID = nbt.getString("targetID");
        entityTags = nbt.getCompoundTag("targetNBT");
        ignoreEntityNBT = nbt.getBoolean("ignoreTargetNBT");
        entitySubtypes = nbt.getBoolean("targetSubtypes");
        
        useMainHand = nbt.getBoolean("allowMainHand");
        useOffHand = nbt.getBoolean("allowOffHand");
        required = nbt.getInteger("requiredUses");
        onInteract = nbt.getBoolean("onInteract");
        onHit = nbt.getBoolean("onHit");
    }
	
	public void setUserProgress(UUID uuid, int progress)
	{
		userProgress.put(uuid, progress);
	}
	
	public int getUsersProgress(UUID uuid)
	{
        Integer n = userProgress.get(uuid);
        return n == null? 0 : n;
	}
}
