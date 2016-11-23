package bq_standard.client.gui.editors.callback;

import com.google.gson.JsonObject;
import betterquesting.api.other.ICallback;

public class JsonObjectCallback implements ICallback<JsonObject>
{
	private final JsonObject baseJson;
	
	public JsonObjectCallback(JsonObject json)
	{
		this.baseJson = json;
	}
	
	@Override
	public void setValue(JsonObject value)
	{
		if(value != baseJson)
		{
			baseJson.entrySet().clear();
			baseJson.entrySet().addAll(value.entrySet());
		}
	}
}
