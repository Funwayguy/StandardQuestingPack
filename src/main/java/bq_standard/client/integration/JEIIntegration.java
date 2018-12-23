package bq_standard.client.integration;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocus.Mode;
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
			Mode m = mouseClick != 1 ? Mode.OUTPUT : Mode.INPUT;
			BQStandardJEI.jei.getRecipesGui().show(new Focus<>(obj, m));
		}
	}
	
	private static class Focus<T> implements IFocus<T>
	{
		private T value;
		private Mode mode;
		
		public Focus(T value, Mode mode)
		{
			this.value = value;
			this.mode = mode;
		}
		@Override
		public T getValue()
		{
			return value;
		}

		@Override
		public Mode getMode()
		{
			return mode;
		}
	}
	
	@JEIPlugin
	public static class BQStandardJEI implements IModPlugin
	{
		public static IJeiRuntime jei;
		@Override
		public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
		{
			jei = jeiRuntime;
		}
	}
}
