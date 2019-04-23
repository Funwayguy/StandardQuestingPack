package bq_standard.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.cache.CapabilityProviderQuestCache;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import bq_standard.client.gui.editors.tasks.GuiEditTaskAdvancement;
import bq_standard.client.gui.tasks.PanelTaskAdvancement;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskAdvancement;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.PlayerAdvancements;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TaskAdvancement implements ITask
{
	private final List<UUID> completeUsers = new ArrayList<>();
	public ResourceLocation advID;
    
    @Override
    public String getUnlocalisedName()
    {
        return "bq_standard.task.advancement";
    }
    
    @Override
    public ResourceLocation getFactoryID()
    {
        return FactoryTaskAdvancement.INSTANCE.getRegistryName();
    }
    
    public void onAdvancementGet(IQuest quest, EntityPlayer player, Advancement advancement)
    {
        if(advancement == null || advID == null || !advID.equals(advancement.getId())) return;
        detect(player, quest);
    }
    
    @Override
    public void detect(EntityPlayer player, IQuest quest)
    {
        if(!(player instanceof EntityPlayerMP) || player.getServer() == null || advID == null) return;
        
		UUID playerID = QuestingAPI.getQuestingUUID(player);
        
        Advancement adv = player.getServer().getAdvancementManager().getAdvancement(advID);
        PlayerAdvancements playerAdv = player.getServer().getPlayerList().getPlayerAdvancements((EntityPlayerMP)player);
        
        if(adv == null) return;
        
        if(playerAdv.getProgress(adv).isDone()) setComplete(playerID);
        
        QuestCache qc = player.getCapability(CapabilityProviderQuestCache.CAP_QUEST_CACHE, null);
        if(qc != null) qc.markQuestDirty(QuestingAPI.getAPI(ApiReference.QUEST_DB).getID(quest));
    }
    
    @Override
    public boolean isComplete(UUID uuid)
    {
        return completeUsers.contains(uuid);
    }
    
    @Override
    public void setComplete(UUID uuid)
    {
        if(!completeUsers.contains(uuid)) completeUsers.add(uuid);
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
    
    @Nullable
    @Override
	@SideOnly(Side.CLIENT)
    public IGuiPanel getTaskGui(IGuiRect rect, IQuest quest)
    {
        return new PanelTaskAdvancement(rect, quest, this);
    }
    
    @Override
    @Nullable
	@SideOnly(Side.CLIENT)
    public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
    {
        return new GuiEditTaskAdvancement(parent, quest, this);
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
        nbt.setString("advancement_id", advID == null ? "" : advID.toString());
        return nbt;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        String id = nbt.getString("advancement_id");
        advID = StringUtils.isNullOrEmpty(id) ? null : new ResourceLocation(id);
    }
}
