package bq_standard.client.gui.editors.callback;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.ICallback;
import betterquesting.api.misc.INBTSaveLoad;
import net.minecraft.nbt.NBTBase;

public class JsonSaveLoadCallback<T extends NBTBase> implements ICallback<T>
{
	private final INBTSaveLoad<T> saveLoad;
	
	public JsonSaveLoadCallback(INBTSaveLoad<T> saveLoad)
	{
		this.saveLoad = saveLoad;
	}
	
	@Override
	public void setValue(T value)
	{
		saveLoad.readFromNBT(value, EnumSaveType.CONFIG);
	}
}
