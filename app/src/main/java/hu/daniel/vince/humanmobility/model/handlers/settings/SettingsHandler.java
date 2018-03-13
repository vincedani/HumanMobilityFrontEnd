package hu.daniel.vince.humanmobility.model.handlers.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Ferenc Lakos.
 * Date: 2016. 01. 22.
 */

public class SettingsHandler {

    // region Members

    private static SettingsHandler instance;
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    // endregion

    // region Constructors

    private SettingsHandler(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = preferences.edit();

    }

    // endregion

    // region Instance

    public static SettingsHandler getInstance(Context context) {
        if(instance == null)
            synchronized (SettingsHandler.class){
                if(instance == null)
                    instance = new SettingsHandler(context);
            }
        return instance;
    }

    // endregion

    // region Save methods

    public void save(String key, int value) {
        editor.putInt(key, value).apply();
    }

    public void save(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }

    public void save(String key, long value) {
        editor.putLong(key, value).apply();
    }

    // endregion

    // region Delete methods

    public void remove(String key) {
        editor.remove(key).apply();
    }

    // endregion

    // region Getter methods

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public long getLong(String key) {
        return getLong(key, 0L);
    }

    public long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    // endregion
}
