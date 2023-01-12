package com.example.doorlock.user;

import android.content.Context;

import com.example.doorlock.sharedPreferences.SharedPreferencesManager;

import java.io.Serializable;

public class User{

    private final UserAuthorizationRequest authorization;
    //private SharedPreferencesManager preferencesManager;
    private Context context;

    public User(Context context){
        this.context = context;
        authorization = new UserAuthorizationRequest(context);
        //preferencesManager = new SharedPreferencesManager(context);
    }

    public UserAuthorizationRequest getAuthorization() {
        return authorization;
    }


}
