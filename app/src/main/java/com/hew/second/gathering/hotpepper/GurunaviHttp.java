package com.hew.second.gathering.hotpepper;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class GurunaviHttp {
    private static Retrofit retrofit = null;
    private static GurunaviApiService service = null;

    private static OkHttpClient.Builder httpClient = null;

    protected static OkHttpClient.Builder getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    //header設定
                    Request request = original.newBuilder()
                            .method(original.method(), original.body())
                            .build();

                    okhttp3.Response response = chain.proceed(request);

                    return response;
                }
            });
        }
        return httpClient;
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://api.gnavi.co.jp/")
                    .client(getHttpClient().build())
                    .build();
        }

        return retrofit;
    }


    public static GurunaviApiService getService() {
        if (service == null) {
            service = getRetrofit().create(GurunaviApiService.class);
        }

        return service;
    }


}
