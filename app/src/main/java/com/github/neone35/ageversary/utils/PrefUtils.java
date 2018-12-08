package com.github.neone35.ageversary.utils;

import android.content.SharedPreferences;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import androidx.annotation.NonNull;


public class PrefUtils {

    public static final String PREF_FILE_NAME = "ageversary_user_prefs";
    @Nullable
    private static PrefUtils INSTANCE;
    private final SharedPreferences preferences;

    private PrefUtils(SharedPreferences sharedPreferences) {
        preferences = sharedPreferences;
    }

    public static PrefUtils getInstance(@NonNull SharedPreferences sharedPreferences) {
        if (INSTANCE == null) {
            INSTANCE = new PrefUtils(sharedPreferences);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public void putString(@Nonnull String key, @Nonnull String value) {
        preferences.edit().putString(key, value).apply();
    }

    public String getString(@Nonnull String key) {
        return preferences.getString(key, "");
    }

    public void putBoolean(@Nonnull String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(@Nonnull String key) {
        return preferences.getBoolean(key, false);
    }

    public void putInt(@Nonnull String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public int getInt(@Nonnull String key) {
        return preferences.getInt(key, -1);
    }

    public void clear() {
        preferences.edit().clear().apply();
    }

}
