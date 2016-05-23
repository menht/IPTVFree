package it.michelelacorte.iptvfree.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is an Util for save user preferences on device.
 *
 * Created by Michele on 30/04/2016
 */
public class SharedPreference {
    private final static String IPTV_PREFERENCES = "IPTVFreePreferences";

    /**
     * Public constructor.
     */
    public SharedPreference() {
        super();
    }

    /**
     * This method save user preference on device.
     * @param context Context
     * @param KEY String
     * @param favorites List<String>
     */
    public void savePreferencesData(Context context, String KEY, List<String> favorites) {
        SharedPreferences settings;
        Editor editor;
        settings = context.getSharedPreferences(IPTV_PREFERENCES,Context.MODE_PRIVATE);
        editor = settings.edit();
        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);
        editor.putString(KEY, jsonFavorites);
        editor.apply();
    }

    /**
     * Load saved preference.
     * @param context Context
     * @param KEY String
     * @return ArrayList<String>
     */
    public ArrayList<String> loadPreferencesData(Context context, String KEY) {
        SharedPreferences settings;
        List<String> favorites;
        settings = context.getSharedPreferences(IPTV_PREFERENCES,Context.MODE_PRIVATE);
        if (settings.contains(KEY)) {
            String jsonFavorites = settings.getString(KEY, null);
            Gson gson = new Gson();
            String[] favoriteItems = gson.fromJson(jsonFavorites,String[].class);
            favorites = Arrays.asList(favoriteItems);
            favorites = new ArrayList<String>(favorites);
        } else
            return null;
        return new ArrayList<String>(favorites);
    }

    /**
     * This method save URL if user load custom playlist
     * @param context Context
     * @param KEY String
     * @param URL String
     */
    public void savePreferencesURL(Context context, String KEY, String URL) {
        SharedPreferences settings;
        Editor editor;
        settings = context.getSharedPreferences(IPTV_PREFERENCES,Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putString(KEY, URL);
        editor.apply();
    }

    /**
     * This method restore URL from device
     * @param context Context
     * @param KEY String
     * @return String
     */
    public String loadPreferencesURL(Context context, String KEY)
    {
        SharedPreferences settings;
        String URL = null;
        settings = context.getSharedPreferences(IPTV_PREFERENCES,Context.MODE_PRIVATE);
        if (settings.contains(KEY)) {
            URL = settings.getString(KEY, null);
        }
        return URL;
    }
}