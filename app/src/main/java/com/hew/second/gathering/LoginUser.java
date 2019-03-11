package com.hew.second.gathering;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.hew.second.gathering.activities.MainActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.TokenRefreshAuthenticator;
import com.hew.second.gathering.api.Util;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginUser {

    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_UNIQUE_ID = "unique_id";

    private static int id = -1;
    private static String uniqueId = "";
    private static String email = "";
    private static String username = "";
    private static String password = "";
    private static String token = "";

    private final static Object lockObj = new Object();

    public static int getId() {
        return id;
    }

    public static void setId(int id) {
        LoginUser.id = id;
    }

    public static String getPassword(@Nullable SharedPreferences sharedPref) {
        if (sharedPref == null)
            return LoginUser.password;
        LoginUser.password = sharedPref.getString(KEY_PASSWORD, "");
        return LoginUser.password;
    }

    public static String getToken() {
        synchronized (LoginUser.lockObj) {
            // クリティカルセッション

            return "Bearer " + token;
        }
    }

    public static void setToken(String token) {
        synchronized (LoginUser.lockObj) {
            LoginUser.token = token;
        }
    }

    public static String getUniqueId(@Nullable SharedPreferences sharedPref) {
        if (sharedPref == null)
            return LoginUser.uniqueId;
        LoginUser.uniqueId = sharedPref.getString(KEY_UNIQUE_ID, "");
        return LoginUser.uniqueId;
    }

    public static void setUniqueId(@Nullable SharedPreferences sharedPref,String uniqueId) {
        if (sharedPref == null) {
            LoginUser.uniqueId = uniqueId;
            return;
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        LoginUser.uniqueId = uniqueId;
        editor.putString(KEY_UNIQUE_ID, uniqueId);
        editor.apply();
    }

    public static String getUsername(@Nullable SharedPreferences sharedPref) {
        if (sharedPref == null)
            return LoginUser.username;
        LoginUser.username = sharedPref.getString(KEY_USERNAME, "");
        return LoginUser.username;
    }

    public static void setUsername(@Nullable SharedPreferences sharedPref, String username) {
        if (sharedPref == null) {
            LoginUser.username = username;
            return;
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        LoginUser.username = username;
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public static void setEmail(@Nullable SharedPreferences sharedPref, String email) {
        if (sharedPref == null) {
            LoginUser.email = email;
            return;
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        LoginUser.email = email;
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public static String getEmail(@Nullable SharedPreferences sharedPref) {
        if (sharedPref == null)
            return LoginUser.email;
        LoginUser.email = sharedPref.getString(KEY_EMAIL, "");
        return LoginUser.email;
    }

    public static void setUserInfo(SharedPreferences sharedPref, String email, String password, String token) {
        SharedPreferences.Editor editor = sharedPref.edit();
        LoginUser.token = token;
        LoginUser.email = email;
        LoginUser.password = password;
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    public static void deleteUserInfo(SharedPreferences sharedPref) {
        SharedPreferences.Editor editor = sharedPref.edit();
        LoginUser.token = "";
        LoginUser.email = "";
        LoginUser.password = "";
        editor.clear();
        editor.apply();
    }

    private static Retrofit retrofit = null;
    private static LoginApiService service = null;
    private static OkHttpClient.Builder httpClient = null;

    protected static OkHttpClient getHttpClientWithHeader() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    //header設定
                    Request request = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json")
                            .method(original.method(), original.body())
                            .build();

                    okhttp3.Response response = chain.proceed(request);

                    return response;
                }
            });
        }
        return httpClient.build();
    }

    protected static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://laravelv2-dot-eventer-1543384121468.appspot.com/")
                    .client(LoginUser.getHttpClientWithHeader())
                    .build();
        }

        return retrofit;
    }

    public static synchronized LoginApiService getService() {
        if (LoginUser.service == null) {
            service = getRetrofit().create(LoginApiService.class);
        }

        return service;
    }

}
