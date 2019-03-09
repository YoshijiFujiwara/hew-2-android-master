package com.hew.second.gathering.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Util {

    public static final String PREF_FILE_NAME = "com.hew.second.gathering.preferences";
    private static Retrofit retrofit = null;
    private static ApiService service = null;
    private static OkHttpClient.Builder httpClient = null;
    private static SharedPreferences sp = null;
    public static File cacheDir = null;

    protected static OkHttpClient getHttpClientWithHeader() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .retryOnConnectionFailure(false)
                    .authenticator(new TokenRefreshAuthenticator());
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    //header設定
                    Request request = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json")
                            .header("Authorization", LoginUser.getToken())
                            .method(original.method(), original.body())
                            .build();

                    okhttp3.Response response = chain.proceed(request);

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
                    .baseUrl("https://laravelv2-dot-eventer-1543384121468.appspot.com/")
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
    }

    public static SharedPreferences getSharedPref() {
        return sp;
    }
}
