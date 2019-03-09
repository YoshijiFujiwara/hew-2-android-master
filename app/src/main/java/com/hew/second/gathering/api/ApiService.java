package com.hew.second.gathering.api;

import java.util.HashMap;

import io.reactivex.Completable;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
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
    @POST("api/auth/refresh")
    Observable<JWT> getRefreshToken();

    @POST("api/auth/me")
    Observable<ProfileDetail> getProfile();

    /**
     * push通知用のデバイスIDのやりとり系API
     */
    @POST("api/device_token")
    Observable<DeviceTokenDetail> storeDeviceToken(@Body HashMap<String, String> body);

    /*
     * 友達系API
     */
    @GET("api/friends")
    Observable<FriendList> getFriendList();

    @POST("api/friends")
    Observable<Friend> requestAddFriend(@Body HashMap<String, String> body);

    @GET("api/friends/waiting")
    Observable<FriendList> getApplyingFriendList();

    @GET("api/friends/requested")
    Observable<FriendList> getPendedFriendList();

    @POST("api/friends/permit")
    Completable permitFriendRequest(@Body HashMap<String, Integer> body);

    @POST("api/friends/reject")
    Completable rejectFriendRequest(@Body HashMap<String, Integer> body);

    @GET("api/friends/{friend}")
    Observable<FriendDetail> getFriendDetail(@Path("friend") int userId);

    @DELETE("api/friends/{friend}")
    Completable deleteFriend(@Path("friend") int userId);

    @PUT("api/friends/{friend}/cancel_invitation")
    Completable cancelFriendInvitation(@Path("friend") int userId);

    @PUT("api/friends/{friend}/attribute")
    Observable<FriendDetail> updateFriendAttribute(@Path("friend") int userId, @Body HashMap<String, String> body);
    
    /*
     * グループ系API
     */
    @GET("api/groups")
    Observable<GroupList> getGroupList();

    @GET("api/groups/{group}")
    Observable<GroupDetail> getGroupDetail(@Path("group") int groupId);

    @POST("api/groups")
    Observable<GroupDetail> createGroup(@Body HashMap<String, String> body);

    @PUT("api/groups/{group}")
    Observable<GroupDetail> updateGroupName(@Path("group") int groupId, @Body HashMap<String, String> body);

    @DELETE("api/groups/{group}/users/{user}")
    Completable deleteGroupUser(@Path("group") int groupId, @Path("user") int userId);

    @DELETE("api/groups/{group}")
    Completable deleteGroup(@Path("group") int groupId);

    @POST("api/groups/{group}/users")
    Completable addUserToGroup(@Path("group") int groupId, @Body HashMap<String, Integer> body);

    /*
     * セッション系API
     */
    @GET("api/sessions")
    Observable<SessionList> getSessionList();

    @GET("api/sessions/not_start")
    Observable<SessionList> getSessionNotStartList();

    @GET("api/sessions/on_going")
    Observable<SessionList> getSessionOnGoingList();

    @GET("api/sessions/not_payment_complete")
    Observable<SessionList> getSessionNotPaymentComplete();

    @GET("api/sessions/history")
    Observable<SessionList> getSessionHistory();

    @GET("api/sessions/complete")
    Observable<SessionList> getSessionComplete();

    @POST("api/sessions")
    Observable<SessionDetail> createSession(@Body HashMap<String, String> body);

    @GET("api/sessions/{session}")
    Observable<SessionDetail> getSessionDetail(@Path("session") int sessionId);

    @PUT("api/sessions/{session}")
    Observable<SessionDetail> updateSession(@Path("session") int sessionId, @Body HashMap<String, String> body);

    @DELETE("api/sessions/{session}")
    Completable deleteSession(@Path("session") int sessionId);

    /*
     * セッションユーザー系API
     */
    @GET("api/sessions/{session}/users")
    Observable<SessionUserList> getSessionUserList(@Path("session") int sessionId);

    @PUT("api/sessions/{session}/users/{user}")
    Observable<SessionUserDetail> updateSessionUser(@Path("session") int sessionId, @Path("user") int userId, @Body HashMap<String, String> body);

    @PUT("api/sessions/{session}/users/{user}/switch_paid")
    Observable<SessionUserDetail> sessionUserSwitchPaid(@Path("session") int sessionId, @Path("user") int userId);

    @POST("api/sessions/{session}/users")
    Observable<SessionUserList> createSessionUser(@Path("session") int sessionId, @Body HashMap<String, String> body);

    @POST("api/sessions/{session}/groups/{group}")
    Observable<SessionUserList> createSessionGroup(@Path("session") int sessionId, @Path("group") int groupId);

    @DELETE("api/sessions/{session}/users/{user}")
    Completable deleteSessionUser(@Path("session") int sessionId, @Path("user") int userId);


    /*
     * デフォルト設定系API
     */
    @GET("api/default_settings")
    Observable<DefaultSettingList> getDefaultSettingList();

    @GET("api/default_settings/{default_setting}")
    Observable<DefaultSettingDetail> getDefaultSettingDetail(@Path("default_setting") int defaultSettingId);

    @PUT("api/default_settings/{default_setting}")
    Observable<DefaultSettingDetail> updateDefaultSettingName(@Path("default_setting") int defaultSettingId, @Body HashMap<String, String> body);

    @POST("api/default_settings")
    Observable<DefaultSettingDetail> createDefaultSetting(@Body HashMap<String, String> body);

    @DELETE("api/default_settings/{default_setting}")
    Completable deleteDefaultSetting(@Path("default_setting") int defaultSettingId);

    @POST("api/search/forward_by_username")
    Observable<FriendList> searchAddableFriendList(@Body HashMap<String, String> body);

    /**
     * 属性系API
     */
    @GET("api/attributes")
    Observable<AttributeList> getAttributeList();

    @POST("api/attributes")
    Observable<AttributeDetail> createAttribute(@Body HashMap<String, String> body);

    @GET("api/attributes/{attribute}")
    Observable<AttributeDetail> getAttributeDetail(@Path("attribute") int attributeId);

    @PUT("api/attributes/{attribute}")
    Observable<AttributeDetail> updateAttribute(@Path("attribute") int attributeId, @Body HashMap<String, String> body);

    @DELETE("api/attributes/{attribute}")
    Completable deleteAttribute(@Path("attribute") int attributeId);


    /*
     * ゲスト系API
     */
    @GET("api/guest/sessions")
    Observable<SessionList> getGuestSessionList();
    @GET("api/guest/sessions/wait")
    Observable<SessionList> getGuestSessionWaitList();
    @GET("api/guest/sessions/allow")
    Observable<SessionList> getGuestSessionAllowList();
    @PUT("api/guest/sessions/{session}")
    Observable<SessionDetail> updateGuestSession(@Path("session") int sessionId, @Body HashMap<String, String> body);
    @GET("api/guest/sessions/{session}")
    Observable<SessionDetail> getGuestSessionDetail(@Path("session") int sessionId);

    /*
     * ユーザー検索系API
     */
    @GET("api/search/can_add_friend_users")
    Observable<FriendList> getAddableFriendList();

    @GET("api/groups/{group}/users/can_add")
    Observable<FriendList> getAddableToGroupFriendList(@Path("group") int groupId);

    @GET("api/sessions/{session}/users/can_add")
    Observable<FriendList> getAddableToSessionFriendList(@Path("session") int sessionId);

    @GET("api/sessions/{session}/groups/can_add")
    Observable<GroupList> getAddableToSessionGroupList(@Path("session") int sessionId);

    /*
    * ユーザープロフィール系API
    */
    @PUT("api/profile/update")
    Observable<ProfileDetail> updateProfileUser(@Body HashMap<String, String> body);

    @POST("api/hotpepper/recommend")
    Observable<ShopIdList> getRecommendShopIdList(@Body HashMap<String, HashMap<String, String>> body, @Query("count") int count);

}
