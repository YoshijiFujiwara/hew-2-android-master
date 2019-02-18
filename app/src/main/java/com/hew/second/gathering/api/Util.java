package com.hew.second.gathering.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.R;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Util {

    public static final String PREF_FILE_NAME = "com.hew.second.gathering.preferences";
    private static Retrofit retrofit = null;
    private static ApiService service = null;
    private static OkHttpClient.Builder httpClient = null;

    public static boolean isLoading() {
        return loading;
    }

    public static void setLoading(boolean loading, Activity activity, int color) {
        /*
        try{
            View loadingView = activity.findViewById(R.id.loading_view);
            ProgressBar loadingProgressBar = activity.findViewById(R.id.loading_progressBar);
            loadingView.setBackgroundColor(color);
            if(loading){
                loadingView.setVisibility(View.VISIBLE);
                loadingProgressBar.setVisibility(View.VISIBLE);
            }else{
                loadingView.setVisibility(View.GONE);
                loadingProgressBar.setVisibility(View.GONE);
            }
            Util.loading = loading;
        }catch (Exception e){
            Log.d("loading","不正な呼び出しです。");
        }
        */
    }

    public static void setLoading(boolean loading, Activity activity) {
        setLoading(loading, activity , Color.argb(100,100,100,100));
    }

    private static boolean loading = false;

    protected static OkHttpClient.Builder getHttpClientWithHeader() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder().authenticator(new TokenRefreshAuthenticator());
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
        return httpClient;
    }

    protected static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://laravelv2-dot-eventer-1543384121468.appspot.com/")
                    .client(Util.getHttpClientWithHeader().build())
                    .build();
        }

        return retrofit;
    }

    public static ApiService getService() {
        if (Util.service == null) {
            service = getRetrofit().create(ApiService.class);
        }

        return service;
    }
}
