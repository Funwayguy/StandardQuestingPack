package bq_standard.importers.ftbq;

public class FTBEntry
{
    public final int id;
    public final FTBEntryType type;
    
    public FTBEntry(int id, FTBEntryType type)
    {
        this.id = id;
        this.type = type;
    }
    
    public enum FTBEntryType
    {
        QUEST,
        LINE,
        VAR
    }
}
