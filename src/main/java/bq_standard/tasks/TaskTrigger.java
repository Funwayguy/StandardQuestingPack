package bq_standard.tasks;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.storage.DBEntry;
import bq_standard.advancment_hacks.BqsAdvListener;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskTrigger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskTrigger implements ITaskTickable
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
	private final List<UUID> completeUsers = new ArrayList<>();
    
    private String triggerID = "minecraft:impossible";
    private String critJson = "{}";
    private BqsAdvListener listener = null;
    private boolean needsSetup = true;
    
    public String desc = "";
    
    public String getCriteriaJson()
    {
        return this.critJson;
    }
    
    public void setCriteriaJson(String json)
    {
        if(critJson.equals(json)) return;
        critJson = StringUtils.isNullOrEmpty(json) ? "{}" : json;
        try
        {
            GSON.fromJson(json, JsonObject.class);
        } catch(Exception e)
        {
            BQ_Standard.logger.error("Unable to parse JSON for trigger task");
            critJson = "{}";
        }
        needsSetup = true;
    }
    
    public String getTriggerID()
    {
        return this.triggerID;
    }
    
    public void setTriggerID(String id)
    {
        if(this.triggerID.equals(id)) return;
        this.triggerID = id;
        needsSetup = true;
    }
    
    @SuppressWarnings("unchecked")
    private void setupListener(DBEntry<IQuest> quest)
    {
        this.needsSetup = false; // Even if this fails, we're not going to try again till something changed.
        
        int tskID = quest.getValue().getTasks().getID(this);
        
        ICriterionTrigger trig = CriteriaTriggers.get(new ResourceLocation(triggerID));
        if(trig == null) return;
        
        try
        {
            ICriterionInstance in = trig.deserializeInstance(GSON.fromJson(critJson, JsonObject.class), null);
            listener = new BqsAdvListener(trig, in, quest, new DBEntry<>(tskID, this));
        } catch(Exception e)
        {
        
        }
    }
    
    public BqsAdvListener<?> getListener()
    {
        return this.listener;
    }
    
    public boolean hasSetup()
    {
        return !this.needsSetup;
    }
    
    public void onCriteriaComplete(DBEntry<IQuest> quest, EntityPlayerMP player, BqsAdvListener advList)
    {
        if(advList != this.listener) return;
        UUID playerID = QuestingAPI.getQuestingUUID(player);
        setComplete(playerID);
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        if(qc != null) qc.markQuestDirty(quest.getID());
    }
    
    @Override
    public void tickTask(@Nonnull DBEntry<IQuest> quest, @Nonnull EntityPlayer player) // Used purely to get the task listening
    {
        if(!needsSetup) return;
        setupListener(quest);
    }
    
    @Override
    public String getUnlocalisedName()
    {
		return BQ_Standard.MODID + ".task.trigger";
    }
    
    @Override
    public ResourceLocation getFactoryID()
    {
        return FactoryTaskTrigger.INSTANCE.getRegistryName();
    }
    
    @Override
    public void detect(EntityPlayer player, IQuest quest)
    {
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
    @Nullable
    @SideOnly(Side.CLIENT)
    public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
    {
        return new PanelTextBox(rect, desc).setColor(PresetColor.TEXT_MAIN.getColor());
    }
    
    @Override
    @Nullable
    @SideOnly(Side.CLIENT)
    public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
    {
        return null;
    }
    
    @Override
    public NBTTagCompound writeProgressToNBT(NBTTagCompound nbt, @Nullable List<UUID> users)
    {
		NBTTagList jArray = new NBTTagList();
		for(UUID uuid : completeUsers)
		{
			jArray.appendTag(new NBTTagString(uuid.toString()));
		}
		nbt.setTag("completeUsers", jArray);
		
		return nbt;
    }
    
    @Override
    public void readProgressFromNBT(NBTTagCompound nbt, boolean merge)
    {
		completeUsers.clear();
		NBTTagList cList = nbt.getTagList("completeUsers", 8);
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
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setString("description", desc);
        nbt.setString("trigger", triggerID);
        nbt.setString("conditions", critJson);
        return nbt;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        this.desc = nbt.getString("description");
        this.setTriggerID(nbt.getString("trigger"));
        this.setCriteriaJson(nbt.getString("conditions"));
    }
}
