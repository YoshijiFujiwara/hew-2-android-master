package com.hew.second.gathering.hotpepper;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.hew.second.gathering.Env.HOTPEPPER_API_KEY;
import static com.hew.second.gathering.api.Util.cacheDir;
import static com.hew.second.gathering.api.Util.hasNetwork;

public class HpHttp {
    private static Retrofit retrofit = null;
    private static HpApiService service = null;

    private static OkHttpClient.Builder httpClient = null;

    protected static OkHttpClient.Builder getHttpClient() {
        if (httpClient == null) {
            long cacheSize = (5 * 1024 * 1024);
            Cache myCache = new Cache(cacheDir, cacheSize);
            httpClient = new OkHttpClient.Builder().cache(myCache);
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();

                    HttpUrl url = original.url().newBuilder().addQueryParameter("key", HOTPEPPER_API_KEY.toString()).build();

                    //header設定
                    Request.Builder requestBuilder = original.newBuilder()
                            .url(url)
                            .method(original.method(), original.body());

                    if (hasNetwork()) {
                        requestBuilder.header("Cache-Control", "public, max-age=" + 60 * 60);
                    } else {
                        requestBuilder.header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7);
                    }

                    okhttp3.Response response = chain.proceed(requestBuilder.build());

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
                    .baseUrl("https://webservice.recruit.co.jp/hotpepper/")
                    .client(getHttpClient().build())
                    .build();
        }

        return retrofit;
    }


    public static HpApiService getService() {
        if (service == null) {
            service = getRetrofit().create(HpApiService.class);
        }

        return service;
    }

}
