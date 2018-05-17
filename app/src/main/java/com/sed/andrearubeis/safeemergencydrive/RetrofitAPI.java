package com.sed.andrearubeis.safeemergencydrive;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface RetrofitAPI {

    @GET("getGpsPosition.php")
    Call<Gps> getGpsPosition();

    @GET("getWarningPosition.php")
    Call<List<Gps>> getWarningPosition(@Query("usr_lat") String lat , @Query("usr_lon") String lon);


}