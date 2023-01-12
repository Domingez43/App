package com.example.doorlock.user;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface UserAuthorizationService {

    /*
    TODO: "Odpoved je potom ID,MAC,PASS,ACCESS_LEVEL,DESCRIPTION ; Access_level sa vlastne zakazdym zavolanim sluzby zresetuje
            na 0 cim pouzivatel strati pristup k otvaraniu a bude potrebovat approval v manazmente otvarania.
            PASS je za kazdym zavolanim sluzby pregenerovany nanovo. ID (deviceID) sa recykluje podla MAC."
     */

    @FormUrlEncoded
    @POST("api2-r/iBeacon/data") //
    Call<UserAuthorizationAnswer>authorizationRequest(
            @Field("mac") String mac,
            @Field("description") String description
    );
}
