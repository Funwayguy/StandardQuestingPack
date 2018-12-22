package bq_standard.client.integration;

import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;

@JEIPlugin
public class BQStandardJEI implements IModPlugin
{
	public static IJeiRuntime jei;
	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
	{
		jei = jeiRuntime;
	}
	
	public static void showJEI(Object obj, int mouseClick)
	{
		jei.getRecipesGui().show(new Focus<>(obj, mouseClick != 1));
	}
}
