package com.example.eventer.member.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Util {
    private static Retrofit retrofit = null;
    private static OkHttpClient.Builder httpClient = null;
    private static String token = null;

    public static void setToken(String token) {
        Util.token = "Bearer " + token;
    }

    public static String getToken() {
        return token;
    }

    private static OkHttpClient.Builder  getHttpClientWithHeader(){
        if (httpClient == null){
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
        return httpClient;
    }

    public static Retrofit getRetrofit(){
        if( Util.retrofit == null ){
            retrofit = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://laravel-dot-eventer-1543384121468.appspot.com/")
                    .client(Util.getHttpClientWithHeader().build())
                    .build();
        }

        return retrofit;
    }
}
