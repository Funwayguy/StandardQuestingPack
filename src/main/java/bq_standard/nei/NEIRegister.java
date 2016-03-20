package bq_standard.nei;

import org.apache.logging.log4j.Level;
import bq_standard.core.BQ_Standard;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NEIRegister
{
	public static final NEIRegister instance = new NEIRegister();
	
	public void registerHandler()
	{
    	try
    	{
    		BQ_Standard.logger.log(Level.INFO, "Registered NEI handler for " + BQ_Standard.NAME);
    		codechicken.nei.api.API.registerRecipeHandler(new bq_standard.nei.NEIRewardHandler());
    	} catch(Exception e){}
	}
}
