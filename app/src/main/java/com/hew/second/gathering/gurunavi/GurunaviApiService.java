package com.hew.second.gathering.gurunavi;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface GurunaviApiService {

    @GET("RestSearchAPI/v3/?keyid=6f24a055d5586327db00ffc1e67999a1")
    Observable<Tel> getTel(@QueryMap(encoded=true) Map<String, String> options);
}
