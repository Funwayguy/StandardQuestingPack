package bq_standard.client.integration;

import mezz.jei.api.recipe.IFocus;

public class Focus<T> implements IFocus<T>
{
	private T value;
	private Mode mode;
	
	public Focus(T value, boolean isOutputMode)
	{
		this.value = value;
		this.mode = isOutputMode ? Mode.OUTPUT : Mode.INPUT;;
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
