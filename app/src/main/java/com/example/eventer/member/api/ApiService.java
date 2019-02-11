package com.example.eventer.member.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/auth/login")
    Observable<TokenInfo> getToken(@Query("email") String email, @Query("password") String password);

    @POST("api/auth/refresh")
    Observable<TokenInfo> getRefreshToken(@Header("Authorization") String authorization);

    @GET("api/friends")
    Observable<MemberData> getMemberList(@Header("Authorization") String authorization);

    @GET("api/groups")
    Observable<GroupData> getGroupList(@Header("Authorization") String authorization);

    @GET("api/groups/{group}")
    Observable<GroupDetail> getGroupDetail(@Header("Authorization") String authorization, @Path("group") int groupId);

    @PUT("api/groups/{group}")
    Observable<GroupDetail> updateGroupName(@Header("Authorization") String authorization, @Path("group") int groupId, @Body HashMap<String, String> body);

}
