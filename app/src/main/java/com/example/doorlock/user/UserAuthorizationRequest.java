package com.example.doorlock.user;

import android.content.Context;
import android.util.Log;

import com.example.doorlock.sharedPreferences.SharedPreferencesKeys;
import com.example.doorlock.sharedPreferences.SharedPreferencesManager;

import java.io.Serializable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserAuthorizationRequest implements Serializable {

    private final UserAuthorizationService api;
    private SharedPreferencesManager preferences;
    private Context context;

    public UserAuthorizationRequest(Context context) {
        this.context = context;
        api = createRetrofit().create(UserAuthorizationService.class);
        preferences = new SharedPreferencesManager(this.context);
    }

    public void authorizationRequest(){
        final StringBuilder returnVal = new StringBuilder();

        Call<UserAuthorizationAnswer> call = api.authorizationRequest("C7-46-48-8D-C2-C7","RandomMacAddress");

        call.enqueue(new Callback<UserAuthorizationAnswer>() {
            @Override
            public void onResponse(Call<UserAuthorizationAnswer> call, Response<UserAuthorizationAnswer> response) {
                if(!response.isSuccessful()){
                    returnVal.append(response.code());
                    Log.e("API NOT SUCCESS",String.valueOf(response.code()));
                    return;
                }

                UserAuthorizationAnswer userResponse = response.body();
                StringBuilder sb = new StringBuilder();
                sb.append("Code: ").append(response.code()).append("\n");
                sb.append("Status: ").append(userResponse.getStatus()).append("\n");
                sb.append("ID: ").append(userResponse.getData().getId()).append("\n");
                sb.append("MAC: ").append(userResponse.getData().getMac()).append("\n");
                sb.append("PASS: ").append(userResponse.getData().getPass()).append("\n");
                sb.append("ACCESS_LEVEL: ").append(userResponse.getData().getAccessLvl()).append("\n");
                sb.append("DESCRIPTION ").append(userResponse.getData().getDescription()).append("\n \n");

                Log.e("API IN SUCCESS",String.valueOf(sb));

                saveResponse(response);
                returnVal.append(sb);

            }

            @Override
            public void onFailure(Call<UserAuthorizationAnswer> call, Throwable t) {
                Log.e("API",t.getMessage());
                returnVal.append(t.getMessage());
            }
        });
        Log.e("API LAST",String.valueOf(returnVal));
    }


    private Retrofit createRetrofit(){
        return new Retrofit.Builder()
                .baseUrl("https://devel.ceelabs.com/") //
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void saveResponse(Response<UserAuthorizationAnswer> response){
        preferences.saveBooleanVal(SharedPreferencesKeys.USER_ACTIVATED.getKey(), true);

        preferences.saveIntVal(SharedPreferencesKeys.USER_ID.getKey(), response.body().getData().getId());
        preferences.saveStringVal(SharedPreferencesKeys.USER_MAC.getKey(), response.body().getData().getMac());
        preferences.saveStringVal(SharedPreferencesKeys.USER_PASSWORD.getKey(), response.body().getData().getPass());
        preferences.saveIntVal(SharedPreferencesKeys.USER_ACCESS_LVL.getKey(), response.body().getData().getAccessLvl());
        preferences.saveStringVal(SharedPreferencesKeys.USER_NAME.getKey(), response.body().getData().getDescription());

    }

}
