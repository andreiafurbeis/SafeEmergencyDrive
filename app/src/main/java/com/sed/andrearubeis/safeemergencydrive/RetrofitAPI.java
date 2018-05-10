package com.sed.andrearubeis.safeemergencydrive;

import retrofit2.Call;
import retrofit2.http.GET;



public interface RetrofitAPI {

    @GET("getGpsPosition.php")
    Call<Gps> getGpsPosition();


}