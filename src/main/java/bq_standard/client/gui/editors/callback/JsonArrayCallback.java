package bq_standard.client.gui.editors.callback;

import com.google.gson.JsonArray;
import betterquesting.api.other.ICallback;
import betterquesting.api.utils.JsonHelper;

public class JsonArrayCallback implements ICallback<JsonArray>
{
	private final JsonArray baseJson;
	
	public JsonArrayCallback(JsonArray json)
	{
		this.baseJson = json;
	}
	
	@Override
	public void setValue(JsonArray value)
	{
		if(value != baseJson)
		{
			JsonHelper.GetUnderlyingArray(baseJson).clear();
			baseJson.addAll(value);
		}
	}
}
