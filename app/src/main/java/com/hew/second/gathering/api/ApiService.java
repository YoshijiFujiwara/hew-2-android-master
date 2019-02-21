package com.hew.second.gathering.api;

import java.util.HashMap;

import io.reactivex.Completable;
import io.reactivex.Observable;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    /*
     * 認証系API
     */
    @POST("api/auth/login")
    Observable<JWT> getToken(@Query("email") String email, @Query("password") String password);

    @POST("api/auth/refresh")
    Observable<JWT> getRefreshToken(@Header("Authorization") String authorization);

    @POST("api/auth/me")
    Observable<ProfileDetail> getProfile(@Header("Authorization") String authorization);

    /*
     * 友達系API
     */
    @GET("api/friends")
    Observable<FriendList> getFriendList(@Header("Authorization") String authorization);

    @POST("api/friends")
    Observable<Friend> requestAddFriend(@Header("Authorization") String authorization, @Body HashMap<String, String> body);

    @GET("api/friends/waiting")
    Observable<FriendList> getApplyingFriendList(@Header("Authorization") String authorization);

    @GET("api/friends/requested")
    Observable<FriendList> getPendedFriendList(@Header("Authorization") String authorization);

    @POST("api/friends/permit")
    Completable permitFriendRequest(@Header("Authorization") String authorization, @Body HashMap<String, Integer> body);

    @POST("api/friends/reject")
    Completable rejectFriendRequest(@Header("Authorization") String authorization, @Body HashMap<String, Integer> body);

    @GET("api/friends/{friend}")
    Observable<FriendDetail> getFriendDetail(@Header("Authorization") String authorization, @Path("friend") int userId);

    @DELETE("api/friends/{friend}")
    Completable deleteFriend(@Header("Authorization") String authorization, @Path("friend") int userId);

    /*
     * グループ系API
     */
    @GET("api/groups")
    Observable<GroupList> getGroupList(@Header("Authorization") String authorization);

    @GET("api/groups/{group}")
    Observable<GroupDetail> getGroupDetail(@Header("Authorization") String authorization, @Path("group") int groupId);

    @POST("api/groups")
    Observable<GroupDetail> createGroup(@Header("Authorization") String authorization, @Body HashMap<String, String> body);

    @PUT("api/groups/{group}")
    Observable<GroupDetail> updateGroupName(@Header("Authorization") String authorization, @Path("group") int groupId, @Body HashMap<String, String> body);

    @DELETE("api/groups/{group}/users/{user}")
    Completable deleteGroupUser(@Header("Authorization") String authorization, @Path("group") int groupId, @Path("user") int userId);

    @DELETE("api/groups/{group}")
    Completable deleteGroup(@Header("Authorization") String authorization, @Path("group") int groupId);

    @POST("api/groups/{group}/users")
    Completable addUserToGroup(@Header("Authorization") String authorization, @Path("group") int groupId, @Body HashMap<String, Integer> body);

    /*
     * セッション系API
     */
    @GET("api/sessions")
    Observable<SessionList> getSessionList(@Header("Authorization") String authorization);

    @POST("api/sessions")
    Observable<SessionDetail> createSession(@Header("Authorization") String authorization, @Body HashMap<String, String> body);

    @GET("api/sessions/{session}")
    Observable<SessionDetail> getSessionDetail(@Header("Authorization") String authorization, @Path("session") int sessionId);

    @PUT("api/sessions/{session}")
    Observable<SessionDetail> updateSession(@Header("Authorization") String authorization, @Path("session") int sessionId, @Body HashMap<String, String> body);

    @DELETE("api/sessions/{session}")
    Completable deleteSession(@Header("Authorization") String authorization, @Path("session") int sessionId);

    /*
     * デフォルト設定系API
     */
    @GET("api/default_settings")
    Observable<DefaultSettingList> getDefaultSettingList(@Header("Authorization") String authorization);

    @GET("api/default_settings/{default_setting}")
    Observable<DefaultSettingDetail> getDefaultSettingDetail(@Header("Authorization") String authorization, @Path("default_setting") int defaultSettingId);

    @PUT("api/default_settings/{default_setting}")
    Observable<DefaultSettingDetail> updateDefaultSettingName(@Header("Authorization") String authorization, @Path("default_setting") int defaultSettingId, @Body HashMap<String, String> body);

    @POST("api/default_settings")
    Observable<DefaultSettingDetail> createDefaultSetting(@Header("Authorization") String authorization, @Body HashMap<String, String> body);

    @DELETE("api/default_settings/{default_setting}")
    Completable deleteDefaultSetting(@Header("Authorization") String authorization, @Path("default_setting") int defaultSettingId);

    @POST("api/search/forward_by_username")
    Observable<FriendList> searchAddableFriendList(@Header("Authorization") String authorization, @Body HashMap<String, String> body);

    /**
     * 属性系API
     */
    @GET("api/attributes")
    Observable<AttributeList> getAttributeList(@Header("Authorization") String authorization);

    @POST("api/attributes")
    Observable<AttributeDetail> createAttribute(@Header("Authorization") String authorization, @Body HashMap<String, String> body);

    @GET("api/attributes/{attribute}")
    Observable<AttributeDetail> getAttributeDetail(@Header("Authorization") String authorization, @Path("attribute") int attributeId);

    @PUT("api/attributes/{attribute}")
    Observable<AttributeDetail> updateAttribute(@Header("Authorization") String authorization, @Path("attribute") int attributeId, @Body HashMap<String, String> body);

    @DELETE("api/attributes/{attribute}")
    Completable deleteAttribute(@Header("Authorization") String authorization, @Path("attribute") int attributeId);


    /*
     * ゲスト系API
     */
    @GET("api/guest/sessions")
    Observable<SessionList> getGuestSessionList(@Header("Authorization") String authorization);
    @GET("api/guest/sessions/wait")
    Observable<SessionList> getGuestSessionWaitList(@Header("Authorization") String authorization);
    @GET("api/guest/sessions/allow")
    Observable<SessionList> getGuestSessionAllowList(@Header("Authorization") String authorization);
    @PUT("api/guest/sessions/{session}")
    Observable<SessionDetail> updateGuestSession(@Header("Authorization") String authorization, @Path("session") int sessionId, @Body HashMap<String, String> body);
    @GET("api/guest/sessions/{session}")
    Observable<SessionDetail> getGuestSessionDetail(@Header("Authorization") String authorization, @Path("session") int sessionId);

    /*
     * ユーザー検索系API
     */
    @GET("api/search/can_add_friend_users")
    Observable<FriendList> getAddableFriendList(@Header("Authorization") String authorization);

    @GET("api/groups/{group}/users/can_add")
    Observable<FriendList> getAddableToGroupFriendList(@Header("Authorization") String authorization, @Path("group") int groupId);

}
