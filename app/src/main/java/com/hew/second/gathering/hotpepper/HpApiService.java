package com.hew.second.gathering.hotpepper;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;


public interface HpApiService {

    @GET("gourmet/v1/?format=json&count=30")
    Observable<GourmetResult> getShopList(@QueryMap(encoded=true) Map<String, String> options);

    @GET("genre/v1/?format=json")
    Observable<GenreResult> getGenreList();

}
