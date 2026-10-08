package it.atlanticfluidtech.aftwebbrowser;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class AppConfig {

    public static final String PREFS = "AFT_WEB_BROWSER_CONFIG";
    public static final String KEY_TABS = "tabs";
    public static final String KEY_ORIENTATION = "orientation";
    public static final String ORIENTATION_PORTRAIT = "portrait";
    public static final String ORIENTATION_LANDSCAPE = "landscape";

    public static ArrayList<TabItem> loadTabs(Context context) {
        ArrayList<TabItem> result = new ArrayList<>();

        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String json = p.getString(KEY_TABS, "[]");

        try {
            JSONArray arr = new JSONArray(json == null ? "[]" : json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o != null) result.add(TabItem.fromJson(o));
            }
        } catch (Exception ignored) {
        }

        return result;
    }

    public static void saveTabs(Context context, ArrayList<TabItem> tabs) {
        JSONArray arr = new JSONArray();

        for (TabItem t : tabs) {
            try {
                arr.put(t.toJson());
            } catch (Exception ignored) {
            }
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_TABS, arr.toString())
                .apply();
    }

    public static String loadOrientation(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_ORIENTATION, ORIENTATION_PORTRAIT);
    }

    public static void saveOrientation(Context context, String orientation) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ORIENTATION, orientation)
                .apply();
    }
}
