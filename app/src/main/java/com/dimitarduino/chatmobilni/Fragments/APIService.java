package com.dimitarduino.chatmobilni.Fragments;

import com.dimitarduino.chatmobilni.Izvestuvanja.IResponse;
import com.dimitarduino.chatmobilni.Izvestuvanja.Isprakjac;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAw_eYW6k:APA91bHQ94kBNX7xF6xtbUyqIat4oGJuTa6M0QIdfWrpak-IeQUBHyko0KNOI74CXBosyjzg_ntk6BD5v6C7SMc2MngrUCUjv1tcUqwhvGZbP9v0_S8ZSE7DQZwFU9i2kfpxlXIZ-I__"
    })

    @POST("fcm/send")
    Call<IResponse> sendNotification(@Body Isprakjac body);
}
