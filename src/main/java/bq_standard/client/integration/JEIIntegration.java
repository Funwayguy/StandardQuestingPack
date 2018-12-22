package bq_standard.client.integration;

import net.minecraftforge.fml.common.Loader;

public class JEIIntegration
{
	private static boolean isJeiLoaded;
	
	/**
	 * Displays JEI recipes
	 * 
	 * @param obj ItemStack or FluidStack
	 * @param mouseClick Numeric button: 0 for left, 1 for right, 2 for middle
	 */
	public static void showJEI(Object obj, int mouseClick)
	{
		isJeiLoaded = isJeiLoaded || Loader.isModLoaded("jei");
		if (isJeiLoaded)
		{
			BQStandardJEI.showJEI(obj, mouseClick);
		}
	}
}
