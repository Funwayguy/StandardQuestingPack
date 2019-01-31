package bq_standard.importers.ftbq;

import betterquesting.api.client.importers.IImporter;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.*;
import betterquesting.api.utils.BigItemStack;
import bq_standard.core.BQ_Standard;
import bq_standard.importers.ftbq.FTBEntry.FTBEntryType;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.HashMap;

public class FTBQQuestImporter implements IImporter
{
    public static final FTBQQuestImporter INSTANCE = new FTBQQuestImporter();
    private static final FileFilter FILTER = new FTBQFileFIlter();
    
    @Override
	public String getUnlocalisedName()
	{
		return "bq_standard.importer.ftbq_quest.name";
	}

	@Override
	public String getUnlocalisedDescription()
	{
		return "bq_standard.importer.ftbq_quest.desc";
	}
    
    @Override
    public FileFilter getFileFilter()
    {
        return FILTER;
    }
    
    @Override
    public void loadFiles(IQuestDatabase questDB, IQuestLineDatabase lineDB, File[] files)
    {
        for(File f : files)
        {
            if(f == null || f.getParent() == null) continue;
            
            try(FileInputStream fis = new FileInputStream(f))
            {
                startImport(questDB, lineDB, CompressedStreamTools.readCompressed(fis), f.getParentFile());
            } catch(Exception e)
            {
                BQ_Standard.logger.error("Failed to import FTB Quests NBT file:\n" + f.getAbsolutePath() + "\nReason:", e);
            }
        }
    }
    
    // NOTE: FTBQ shares IDs between multiple types. Check type before use
    protected final HashMap<String, FTBEntry> ID_MAP = new HashMap<>();
    
    private void startImport(IQuestDatabase questDB, IQuestLineDatabase lineDB, NBTTagCompound tagIndex, File folder)
    {
        int[] indexIDs = tagIndex.getIntArray("index");
        ID_MAP.clear();
        
        System.out.println("Found " + indexIDs.length + " quest line(s)");
        for(int id : indexIDs)
        {
            String hexName = Integer.toHexString(id);
            System.out.println("Importing folder '" + Integer.toHexString(id) + "'...");
            
            File qlFolder = new File(folder, hexName);
            if(!qlFolder.exists() || !qlFolder.isDirectory()) continue;
            File[] contents = qlFolder.listFiles();
            if(contents == null) continue;
            
            int lineID = lineDB.nextID();
            IQuestLine questLine = lineDB.createNew(lineID);
            ID_MAP.put(hexName, new FTBEntry(lineID, FTBEntryType.LINE));

            for(File questFile : contents)
            {
                if(!questFile.getName().toLowerCase().endsWith(".nbt")) continue;
                NBTTagCompound qTag;
                try(FileInputStream chFis = new FileInputStream(questFile))
                {
                        qTag = CompressedStreamTools.readCompressed(chFis);
                } catch(Exception e)
                {
                    BQ_Standard.logger.error("Failed to import quest line file entry: " + questFile, e);
                    continue;
                }
                
                if(questFile.getName().equalsIgnoreCase("chapter.nbt"))
                {
                    questLine.setProperty(NativeProps.NAME, qTag.getString("title"));
                    NBTTagList desc = qTag.getTagList("description", 8);
                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < desc.tagCount(); i++)
                    {
                        sb.append(desc.getStringTagAt(i));
                        if(i + 1 < desc.tagCount()) sb.append("\n");
                    }
                    questLine.setProperty(NativeProps.DESC, sb.toString());
                    continue;
                }
    
                int questID = questDB.nextID();
                IQuest quest = questDB.createNew(questID);
                IQuestLineEntry qle = questLine.createNew(questID);
                ID_MAP.put(Integer.toHexString(qTag.getInteger("id")), new FTBEntry(questID, FTBEntryType.QUEST));
                quest.setProperty(NativeProps.NAME, qTag.getString("title"));
                NBTTagList desc = qTag.getTagList("text", 8);
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < desc.tagCount(); i++)
                {
                    sb.append(desc.getStringTagAt(i));
                    if(i + 1 < desc.tagCount()) sb.append("\n");
                }
                quest.setProperty(NativeProps.DESC, sb.toString());
                BigItemStack icoStack = FTBQUtils.convertItem(qTag.getTag("icon"));
                if(!icoStack.getBaseStack().isEmpty()) quest.setProperty(NativeProps.ICON, icoStack); // We're not even going to try and make an equivalent dynamic icon
                qle.setPosition(qTag.getInteger("x") * 24, qTag.getInteger("y") * 24);
            }
        }
    }
}
