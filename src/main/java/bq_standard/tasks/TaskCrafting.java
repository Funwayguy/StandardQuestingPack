package bq_standard.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.party.IParty;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.properties.NativeProps;
import betterquesting.api.quests.tasks.IProgression;
import betterquesting.api.quests.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.ItemComparison;
import betterquesting.api.utils.JsonHelper;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestSettings;
import bq_standard.client.gui.tasks.GuiTaskCrafting;
import bq_standard.core.BQ_Standard;
import bq_standard.tasks.factory.FactoryTaskCrafting;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TaskCrafting implements ITask, IProgression<int[]>
{
	private ArrayList<UUID> completeUsers = new ArrayList<UUID>();
	public ArrayList<BigItemStack> requiredItems = new ArrayList<BigItemStack>();
	public HashMap<UUID, int[]> userProgress = new HashMap<UUID, int[]>();
	boolean partialMatch = true;
	boolean ignoreNBT = false;
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskCrafting.INSTANCE.getRegistryName();
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
	public String getUnlocalisedName()
	{
		return "bq_standard.task.crafting";
	}
	
	@Override
	public void update(EntityPlayer player, IQuest quest)
	{
		if(player.ticksExisted%200 == 0 && !QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE))
		{
			detect(player, quest);
		}
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
		if(isComplete(player.getGameProfile().getId()))
		{
			return;
		}
		
		int[] progress = quest == null || !quest.getProperties().getProperty(NativeProps.GLOBAL)? getPartyProgress(player.getGameProfile().getId()) : getGlobalProgress();
		
		boolean flag = true;
		
		for(int i = 0; i < requiredItems.size(); i++)
		{
			BigItemStack rStack = requiredItems.get(i);
			
			if(progress[i] >= rStack.stackSize)
			{
				continue;
			} else
			{
				flag = false;
				break;
			}
		}
		
		if(flag)
		{
			setComplete(player.getGameProfile().getId());
		}
	}
	
	public void onItemCrafted(IQuest quest, EntityPlayer player, ItemStack stack)
	{
		if(isComplete(player.getGameProfile().getId()))
		{
			return;
		}
		
		int[] progress = getUsersProgress(player.getGameProfile().getId());
		
		for(int i = 0; i < requiredItems.size(); i++)
		{
			BigItemStack rStack = requiredItems.get(i);
			
			if(progress[i] >= rStack.stackSize)
			{
				continue;
			}
			
			if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch) || ItemComparison.OreDictionaryMatch(rStack.oreDict, rStack.GetTagCompound(), stack, !ignoreNBT, partialMatch))
			{
				progress[i] += stack.stackSize;
			}
		}
		
		setUserProgress(player.getGameProfile().getId(), progress);
		
		detect(player, quest);
	}
	
	public void onItemSmelted(IQuest quest, EntityPlayer player, ItemStack stack)
	{
		int[] progress = getUsersProgress(player.getGameProfile().getId());
		
		for(int i = 0; i < requiredItems.size(); i++)
		{
			BigItemStack rStack = requiredItems.get(i);
			
			if(progress[i] >= rStack.stackSize)
			{
				continue;
			}
			
			if(ItemComparison.StackMatch(rStack.getBaseStack(), stack, !ignoreNBT, partialMatch) || ItemComparison.OreDictionaryMatch(rStack.oreDict, rStack.GetTagCompound(), stack, !ignoreNBT, partialMatch))
			{
				progress[i] += stack.stackSize;
			}
		}
		
		setUserProgress(player.getGameProfile().getId(), progress);
		
		detect(player, quest);
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
		
		json.addProperty("partialMatch", partialMatch);
		json.addProperty("ignoreNBT", ignoreNBT);
		
		JsonArray itemArray = new JsonArray();
		for(BigItemStack stack : this.requiredItems)
		{
			itemArray.add(JsonHelper.ItemStackToJson(stack, new JsonObject()));
		}
		json.add("requiredItems", itemArray);
		
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
		
		partialMatch = JsonHelper.GetBoolean(json, "partialMatch", partialMatch);
		ignoreNBT = JsonHelper.GetBoolean(json, "ignoreNBT", ignoreNBT);
		
		requiredItems = new ArrayList<BigItemStack>();
		for(JsonElement entry : JsonHelper.GetArray(json, "requiredItems"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			try
			{
				BigItemStack item = JsonHelper.JsonToItemStack(entry.getAsJsonObject());
				
				if(item != null)
				{
					requiredItems.add(item);
				} else
				{
					continue;
				}
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load quest item data", e);
			}
		}
	}
	
	public void readProgressFromJson(JsonObject json)
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
		
		userProgress = new HashMap<UUID,int[]>();
		for(JsonElement entry : JsonHelper.GetArray(json, "userProgress"))
		{
			if(entry == null || !entry.isJsonObject())
			{
				continue;
			}
			
			UUID uuid;
			try
			{
				uuid = UUID.fromString(JsonHelper.GetString(entry.getAsJsonObject(), "uuid", ""));
			} catch(Exception e)
			{
				BQ_Standard.logger.log(Level.ERROR, "Unable to load user progress for task", e);
				continue;
			}
			
			int[] data = new int[requiredItems.size()];
			JsonArray dJson = JsonHelper.GetArray(entry.getAsJsonObject(), "data");
			for(int i = 0; i < data.length && i < dJson.size(); i++)
			{
				try
				{
					data[i] = dJson.get(i).getAsInt();
				} catch(Exception e)
				{
					BQ_Standard.logger.log(Level.ERROR, "Incorrect task progress format", e);
				}
			}
			
			userProgress.put(uuid, data);
		}
	}
	
	public JsonObject writeProgressToJson(JsonObject json)
	{
		JsonArray jArray = new JsonArray();
		for(UUID uuid : completeUsers)
		{
			jArray.add(new JsonPrimitive(uuid.toString()));
		}
		json.add("completeUsers", jArray);
		
		JsonArray progArray = new JsonArray();
		for(Entry<UUID,int[]> entry : userProgress.entrySet())
		{
			JsonObject pJson = new JsonObject();
			pJson.addProperty("uuid", entry.getKey().toString());
			JsonArray pArray = new JsonArray();
			for(int i : entry.getValue())
			{
				pArray.add(new JsonPrimitive(i));
			}
			pJson.add("data", pArray);
			progArray.add(pJson);
		}
		json.add("userProgress", progArray);
		
		return json;
	}

	@Override
	public void resetUser(UUID uuid)
	{
		completeUsers.remove(uuid);
		userProgress.remove(uuid);
	}

	@Override
	public void resetAll()
	{
		completeUsers.clear();
		userProgress.clear();
	}
	
	@Override
	public float getParticipation(UUID uuid)
	{
		if(requiredItems.size() <= 0)
		{
			return 1F;
		}
		
		float total = 0F;
		
		int[] progress = getUsersProgress(uuid);
		for(int i = 0; i < requiredItems.size(); i++)
		{
			BigItemStack rStack = requiredItems.get(i);
			total += progress[i] / (float)rStack.stackSize;
		}
		
		return total / (float)requiredItems.size();
	}

	@Override
	public IGuiEmbedded getTaskGui(int posX, int posY, int sizeX, int sizeY, IQuest quest)
	{
		return new GuiTaskCrafting(this, quest, posX, posY, sizeX, sizeY);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
	{
		return null;//new guiCraftingEditor(parent, this, quest);
	}
	
	@Override
	public void setUserProgress(UUID uuid, int[] progress)
	{
		userProgress.put(uuid, progress);
	}
	
	@Override
	public int[] getUsersProgress(UUID... users)
	{
		int[] progress = new int[requiredItems.size()];
		
		for(UUID uuid : users)
		{
			int[] tmp = userProgress.get(uuid);
			
			if(tmp == null || tmp.length != requiredItems.size())
			{
				continue;
			}
			
			for(int n = 0; n < progress.length; n++)
			{
				progress[n] += tmp[n];
			}
		}
		
		return progress == null || progress.length != requiredItems.size()? new int[requiredItems.size()] : progress;
	}
	
	public int[] getPartyProgress(UUID uuid)
	{
		int[] total = new int[requiredItems.size()];
		
		IParty party = PartyManager.INSTANCE.getUserParty(uuid);
		
		if(party == null)
		{
			return getUsersProgress(uuid);
		} else
		{
			for(UUID mem : party.getMembers())
			{
				if(mem != null && party.getStatus(mem).ordinal() <= 0)
				{
					continue;
				}

				int[] progress = getUsersProgress(mem);
				
				for(int i = 0; i < progress.length; i++)
				{
					total[i] += progress[i];
				}
			}
		}
		
		return total;
	}
	
	@Override
	public int[] getGlobalProgress()
	{
		int[] total = new int[requiredItems.size()];
		
		for(int[] up : userProgress.values())
		{
			if(up == null)
			{
				continue;
			}
			
			int[] progress = up.length != requiredItems.size()? new int[requiredItems.size()] : up;
			
			for(int i = 0; i < progress.length; i++)
			{
				total[i] += progress[i];
			}
		}
		
		return total;
	}
}
