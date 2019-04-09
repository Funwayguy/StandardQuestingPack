package bq_standard.advancment_hacks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.TaskTrigger;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class BqsAdvListener<T extends ICriterionInstance> extends ICriterionTrigger.Listener<T>
{
    private final ICriterionTrigger<T> trigType;
    private final DBEntry<IQuest> quest;
    private final DBEntry<TaskTrigger> task;
    
    @SuppressWarnings("ConstantConditions")
    public BqsAdvListener(@Nonnull ICriterionTrigger<T> trigType, @Nonnull T critereon, @Nonnull DBEntry<IQuest> quest, @Nonnull DBEntry<TaskTrigger> task)
    {
        super(critereon, null, "BQ_PROXY");
        this.trigType = trigType;
        this.quest = quest;
        this.task = task;
        
        AdvListenerManager.INSTANCE.registerListener(this);
    }
    
    public void registerSelf(PlayerAdvancements playerAdv)
    {
        trigType.addListener(playerAdv, this);
    }
    
    public void unregisterSelf(PlayerAdvancements playerAdv)
    {
        trigType.removeListener(playerAdv, this);
    }
    
    @Override
    public void grantCriterion(PlayerAdvancements playerAdv)
    {
        try
        {
            task.getValue().onCriteriaComplete(quest, ((EntityPlayerMP)f_playerAdv.get(playerAdv)), this);
        } catch(Exception e)
        {
            BQ_Standard.logger.error(e);
        }
    }
    
    //
    public boolean verify()
    {
        IQuest q = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(quest.getID());
        if(q == null) return false;
        ITask t = q.getTasks().getValue(task.getID());
        if(t instanceof TaskTrigger)
        {
            TaskTrigger tCon = (TaskTrigger)t;
            return tCon.getListener() == this;
        }
        
        return false;
    }
    
    @Override
    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass())
        {
            ICriterionTrigger.Listener<?> listener = (ICriterionTrigger.Listener)p_equals_1_;
            return this.getCriterionInstance().equals(listener.getCriterionInstance());
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public int hashCode()
    {
        int i = this.getCriterionInstance().hashCode();
        i = 31 * i;// + this.advancement.hashCode();
        i = 31 * i + "BQ_PROXY".hashCode();
        return i;
    }
    
    private static final Field f_playerAdv;
    
    static
    {
        f_playerAdv = ReflectionHelper.findField(PlayerAdvancements.class, "field_192762_j", "player");
        f_playerAdv.setAccessible(true);
    }
}
