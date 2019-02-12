package com.hew.second.gathering.api;

import java.util.HashMap;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/auth/login")
    Observable<JWT> getToken(@Query("email") String email, @Query("password") String password);

    @POST("api/auth/refresh")
    Observable<JWT> getRefreshToken(@Header("Authorization") String authorization);

    @GET("api/friends")
    Observable<FriendList> getMemberList(@Header("Authorization") String authorization);

    @GET("api/groups")
    Observable<GroupList> getGroupList(@Header("Authorization") String authorization);

    @GET("api/groups/{group}")
    Observable<Group> getGroupDetail(@Header("Authorization") String authorization, @Path("group") int groupId);

    @PUT("api/groups/{group}")
    Observable<Group> updateGroupName(@Header("Authorization") String authorization, @Path("group") int groupId, @Body HashMap<String, String> body);


    @GET("api/sessions")
    Observable<SessionList> getSessionList(@Header("Authorization") String authorization);

    @POST("api/sessions")
    Observable<SessionList> createSession(@Header("Authorization") String authorization);

    @GET("api/sessions/{session}")
    Observable<SessionList> getSessionDetail(@Header("Authorization") String authorization);

    @PUT("api/sessions/{session}")
    Observable<SessionList> updateSession(@Header("Authorization") String authorization);

    @DELETE("api/sessions/{session}")
    Observable<SessionList> deleteSession(@Header("Authorization") String authorization);
}