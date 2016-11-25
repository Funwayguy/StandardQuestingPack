package bq_standard.client.gui.editors.callback;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.ICallback;
import betterquesting.api.misc.IJsonSaveLoad;
import com.google.gson.JsonElement;

public class JsonSaveLoadCallback<T extends JsonElement> implements ICallback<T>
{
	private final IJsonSaveLoad<T> saveLoad;
	
	public JsonSaveLoadCallback(IJsonSaveLoad<T> saveLoad)
	{
		this.saveLoad = saveLoad;
	}
	
	@Override
	public void setValue(T value)
	{
		saveLoad.readFromJson(value, EnumSaveType.CONFIG);
	}
}
