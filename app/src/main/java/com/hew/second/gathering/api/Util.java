package com.hew.second.gathering.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.hew.second.gathering.Env.API_URL;

public class Util {

    public static final String PREF_FILE_NAME = "com.hew.second.gathering.preferences";
    private static Retrofit retrofit = null;
    private static ApiService service = null;
    private static OkHttpClient.Builder httpClient = null;
    private static SharedPreferences sp = null;
    private static ConnectivityManager connectivityManager = null;
    public static File cacheDir = null;

    private Util() {
    }

    protected static OkHttpClient getHttpClientWithHeader() {
        if (httpClient == null) {
            long cacheSize = (5 * 1024 * 1024);
            Cache myCache = new Cache(cacheDir, cacheSize);
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .retryOnConnectionFailure(false)
                    .cache(myCache)
                    .authenticator(new TokenRefreshAuthenticator());
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    //header設定
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json")
                            .header("Authorization", LoginUser.getToken())
                            .method(original.method(), original.body());

                    if (hasNetwork()) {
                        requestBuilder.header("Cache-Control", "public, max-age=" + 3);
                    } else {
                        requestBuilder.header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60);
                    }

                    okhttp3.Response response = chain.proceed(requestBuilder.build());

                    return response;
                }

            });
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(1);
            httpClient.dispatcher(dispatcher);
        }
        return httpClient.build();
    }

    protected static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(API_URL.toString())
                    .client(Util.getHttpClientWithHeader())
                    .build();
        }

        return retrofit;
    }

    public static synchronized ApiService getService() {
        if (Util.service == null) {
            service = getRetrofit().create(ApiService.class);
        }
        return service;
    }

    public static void setSharedPref(@NonNull Activity sp) {
        Util.sp = sp.getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE);
        cacheDir = sp.getCacheDir();
        connectivityManager = (ConnectivityManager) sp.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static SharedPreferences getSharedPref() {
        return sp;
    }

    public static boolean hasNetwork() {
        boolean isConnected = false;
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected())
            isConnected = true;
        return isConnected;
    }
}
