package com.hew.second.gathering.hotpepper;

import com.hew.second.gathering.api.JWT;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface HpApiService {

    @GET("gourmet/v1/?key=372039502fb1977e&format=json&count=50")
    Observable<GourmetResult> getShopList(@QueryMap(encoded=true) HashMap<String, String> options);

    @GET("genre/v1/?key=372039502fb1977e&format=json")
    Observable<GenreResult> getGenreList(@QueryMap(encoded=true) HashMap<String, String> options);
}
