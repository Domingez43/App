package com.example.doorlock.sharedPreferences;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.doorlock.TransmitRules;

import java.io.Serializable;

public class SharedPreferencesManager{

    private final SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;


    public SharedPreferencesManager(Context context){
        this.context = context;
        preferences = this.context.getSharedPreferences("settings",MODE_PRIVATE);
    }

    public int getIntVal(String key, int defaultVal){
        return preferences.getInt(key,defaultVal);
    }

    public String getStringVal(String key, String defaultVal){
        return preferences.getString(key,defaultVal);
    }

    public float getFloatVal(String key, float defaultVal){
        return preferences.getFloat(key,defaultVal);
    }

    public boolean getBooleanVal(String key, boolean defaultVal){
        return preferences.getBoolean(key,defaultVal);
    }

    public void saveIntVal(String key, int val){
        editor = preferences.edit();
        editor.putInt(key,val);
        editor.apply();
    }

    public void saveStringVal(String key, String val){
        editor = preferences.edit();
        editor.putString(key,val);
        editor.apply();
    }

    public void saveFloatVal(String key, float val){
        editor = preferences.edit();
        editor.putFloat(key,val);
        editor.apply();
    }

    public void saveBooleanVal(String key, boolean val){
        editor = preferences.edit();
        editor.putBoolean(key,val);
        editor.apply();
    }
}
