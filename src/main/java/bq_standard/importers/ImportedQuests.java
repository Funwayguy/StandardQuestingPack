package bq_standard.importers;

import betterquesting.api2.storage.BigDatabase;
import betterquesting.api2.storage.DBEntry;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestDatabase;

public class ImportedQuests extends BigDatabase<IQuest> implements IQuestDatabase
{
	private final IQuestDatabase parent;
	
	public ImportedQuests(IQuestDatabase parent)
	{
		this.parent = parent;
	}
	
	@Override
	public NBTTagList writeToNBT(NBTTagList json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		for(DBEntry<IQuest> entry : getEntries())
		{
			NBTTagCompound jq = new NBTTagCompound();
			entry.getValue().writeToNBT(jq, saveType);
			jq.setInteger("questID", entry.getID());
			json.appendTag(jq);
		}
		
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagList json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		reset();
		
		for(int i = 0; i < json.tagCount(); i++)
		{
			NBTBase entry = json.get(i);
			
			if(entry.getId() != 10)
			{
				continue;
			}
			
			NBTTagCompound qTag = (NBTTagCompound)entry;
			
			int qID = qTag.hasKey("questID", 99) ? qTag.getInteger("questID") : -1;
			
			if(qID < 0)
			{
				continue;
			}
			
			IQuest quest = getValue(qID);
			quest = quest != null? quest : this.createNew(qID);
			quest.readFromNBT(qTag, EnumSaveType.CONFIG);
		}
	}
	
	@Override
	public QuestingPacket getSyncPacket()
	{
		return null;
	}
	
	@Override
	public void readPacket(NBTTagCompound payload)
	{
	}
	
	@Override
	public IQuest createNew(int id)
	{
		// TODO: Rewrite this whole importer to just shift IDs on the fly instead of this crap
		IQuest q = parent.createNew(id);
		parent.removeID(id); // Yes I know this is dumb
		add(id, q);
		q.setParentDatabase((IQuestDatabase) this);
		return q;
	}
}
