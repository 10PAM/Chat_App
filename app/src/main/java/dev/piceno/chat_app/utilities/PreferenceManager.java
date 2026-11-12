package dev.piceno.chat_app.utilities;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class contains all the preferences for manipulating database.
 */
public class PreferenceManager {
    private final SharedPreferences sharedPreferences;

    /**
     * Initializes SharedPreferences manager.
     *
     * @param context Gets the context of the PreferenceManager link.
     */
    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Adds key value pair to SharedPreference
     *
     * @param key The key being inputted.
     * @param value The value being inputted.
     */
    public void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Retrieves value of shared preference using key.
     *
     * @param key The key of the value being accessed.
     * @return Returns the stored SharedPreference value.
     */
    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    /**
     * Adds key value pair to SharedPreference
     *
     * @param key The key being inputted.
     * @param value The value being inputted.
     */
    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Retrieves value of shared preference using key.
     *
     * @param key The key of the value being accessed.
     * @return Returns the stored SharedPreference value.
     */
    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    /**
     * Clear shared preferences.
     */
    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
