package bq_standard.importers.hqm.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import bq_standard.importers.hqm.HQMUtilities;
import bq_standard.tasks.TaskBlockBreak;
import bq_standard.tasks.TaskBlockBreak.NbtBlockType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemBlock;

import java.util.ArrayList;
import java.util.List;

public class HQMTaskBlockBreak implements HQMTask
{
    @Override
    public List<ITask> Convert(JsonObject json)
    {
        List<ITask> tasks = new ArrayList<>();
        
        for(JsonElement je : JsonHelper.GetArray(json, "advancements"))
        {
            if(je == null || !je.isJsonObject()) continue;
            JsonObject jAdv = je.getAsJsonObject();
            TaskBlockBreak taskBreak = new TaskBlockBreak();
            
            for(JsonElement je2 : JsonHelper.GetArray(jAdv, "blocks"))
            {
                if(je2 == null || !je2.isJsonObject()) continue;
                JsonObject jBlock = je2.getAsJsonObject();
                BigItemStack stack = HQMUtilities.HQMStackT1(JsonHelper.GetObject(jBlock, "item"));
                if(!(stack.getBaseStack().getItem() instanceof ItemBlock)) continue; // Lazy conversion. Too much effort to handle all the edge cases
                ItemBlock iBlock = (ItemBlock)stack.getBaseStack().getItem();
                NbtBlockType blockType = new NbtBlockType();
                blockType.b = iBlock.getBlock();
                blockType.m = stack.getBaseStack().getItemDamage();
                blockType.n = JsonHelper.GetNumber(jBlock, "required", 1).intValue();
                taskBreak.blockTypes.add(blockType);
            }
            
            tasks.add(taskBreak);
        }
        
        return tasks;
    }
}
