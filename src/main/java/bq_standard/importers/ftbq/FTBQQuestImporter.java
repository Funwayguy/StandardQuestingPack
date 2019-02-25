package bq_standard.importers.ftbq;

import betterquesting.api.client.importers.IImporter;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.*;
import betterquesting.api.questing.rewards.IReward;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.storage.IDatabaseNBT;
import bq_standard.core.BQ_Standard;
import bq_standard.importers.ftbq.FTBEntry.FTBEntryType;
import bq_standard.importers.ftbq.converters.rewards.*;
import bq_standard.importers.ftbq.converters.tasks.*;
import bq_standard.tasks.TaskCheckbox;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.function.Function;

public class FTBQQuestImporter implements IImporter
{
    public static final FTBQQuestImporter INSTANCE = new FTBQQuestImporter();
    private static final FileFilter FILTER = new FTBQFileFIlter();
    
    private static final HashMap<String, Function<NBTTagCompound, ITask[]>> taskConverters = new HashMap<>();
    private static final HashMap<String, Function<NBTTagCompound, IReward[]>> rewardConverters = new HashMap<>();
    
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
    
    // NOTE: FTBQ shares IDs between multiple object types (WHY?!). Check type before use
    protected final HashMap<String, FTBEntry> ID_MAP = new HashMap<>();
    
    private void startImport(IQuestDatabase questDB, IQuestLineDatabase lineDB, NBTTagCompound tagIndex, File folder)
    {
        int[] indexIDs = tagIndex.getIntArray("index"); // Read out the chapter index names
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
                if(!questFile.getName().toLowerCase().endsWith(".nbt")) continue; // No idea why this file is in here
                
                NBTTagCompound qTag; // Read NBT file
                try(FileInputStream chFis = new FileInputStream(questFile))
                {
                        qTag = CompressedStreamTools.readCompressed(chFis);
                } catch(Exception e)
                {
                    BQ_Standard.logger.error("Failed to import quest line file entry: " + questFile, e);
                    continue;
                }
                
                // === CHAPTER INFO ===
                
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
                
                // === QUEST DATA ===
                
                int questID = questDB.nextID();
                IQuest quest = questDB.createNew(questID);
                IQuestLineEntry qle = questLine.createNew(questID);
                ID_MAP.put(Integer.toHexString(qTag.getInteger("id")), new FTBEntry(questID, FTBEntryType.QUEST)); // Add this to the weird ass ID mapping
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
                
                // === IMPORT TASKS ===
                
                NBTTagList taskList = qTag.getTagList("tasks", 10);
                for(int i = 0; i < taskList.tagCount(); i++)
                {
                    NBTTagCompound taskTag = taskList.getCompoundTagAt(i);
                    String tType = taskTag.getString("type");
                    if(!taskConverters.containsKey(tType))
                    {
                        BQ_Standard.logger.warn("Unsupported FTBQ task \"" + tType + "\"! Skipping...");
                        continue;
                    }
                    
                    ITask[] tsks = taskConverters.get(tType).apply(taskTag);
                    
                    if(tsks != null && tsks.length > 0)
                    {
                        IDatabaseNBT<ITask, NBTTagList, NBTTagList> taskReg = quest.getTasks();
                        for(ITask t : tsks) taskReg.add(taskReg.nextID(), t);
                    }
                }
                
                // === IMPORT REWARDS ===
                
                NBTTagList rewardList = qTag.getTagList("rewards", 10);
                for(int i = 0; i < rewardList.tagCount(); i++)
                {
                    NBTTagCompound rewTag = rewardList.getCompoundTagAt(i);
                    String rType = rewTag.getString("type");
                    if(!rewardConverters.containsKey(rType))
                    {
                        BQ_Standard.logger.warn("Unsupported FTBQ reward \"" + rType + "\"! Skipping...");
                        continue;
                    }
                    
                    IReward[] tsks = rewardConverters.get(rType).apply(rewTag);
                    
                    if(tsks != null && tsks.length > 0)
                    {
                        IDatabaseNBT<IReward, NBTTagList, NBTTagList> rewardReg = quest.getRewards();
                        for(IReward t : tsks) rewardReg.add(rewardReg.nextID(), t);
                    }
                }
            }
        }
    }
    
    static
    {
        taskConverters.put("item", new FtbqTaskItem()::convertTask);
        taskConverters.put("fluid", new FtbqTaskFluid()::convertTask);
        taskConverters.put("forge_energy", new FtbqTaskEnergy()::converTask);
        taskConverters.put("xp", new FtbqTaskXP()::convertTask);
        taskConverters.put("dimension", new FtbqTaskDimension()::converTask);
        taskConverters.put("stat", new FtbqTaskStat()::convertTask);
        taskConverters.put("kill", new FtbqTaskKill()::convertTask);
        taskConverters.put("location", new FtbqTaskLocation()::convertTask);
        taskConverters.put("checkmark", tag -> new ITask[]{new TaskCheckbox()});
        
        rewardConverters.put("item", new FtbqRewardItem()::convertTask);
        rewardConverters.put("xp", new FtbqRewardXP(false)::convertTask);
        rewardConverters.put("xp_levels", new FtbqRewardXP(true)::convertTask);
        rewardConverters.put("command", new FtbqRewardCommand()::convertReward);
    }
}
