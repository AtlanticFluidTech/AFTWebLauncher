package it.atlanticfluidtech.aftwebbrowser;

import org.json.JSONException;
import org.json.JSONObject;

public class TabItem {
    public String name;
    public String url;
    public boolean keyboardEnabled;

    public TabItem(String name, String url, boolean keyboardEnabled) {
        this.name = name;
        this.url = url;
        this.keyboardEnabled = keyboardEnabled;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("name", name);
        o.put("url", url);
        o.put("keyboardEnabled", keyboardEnabled);
        return o;
    }

    public static TabItem fromJson(JSONObject o) {
        return new TabItem(
                o.optString("name", "Pagina"),
                o.optString("url", ""),
                o.optBoolean("keyboardEnabled", false)
        );
    }
}
