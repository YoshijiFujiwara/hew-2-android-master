package com.hew.second.gathering;

import com.hew.second.gathering.api.JWT;

import io.reactivex.Observable;
import io.reactivex.observables.ConnectableObservable;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LoginApiService {
    @POST("api/auth/login")
    Observable<JWT> getToken(@Query("email") String email, @Query("password") String password);
}
