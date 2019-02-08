package com.example.eventer.member.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.example.eventer.member.LogUtil;
import com.example.eventer.member.LoginActivity;
import com.example.eventer.member.LoginFragment;
import com.example.eventer.member.R;

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

    private void checkLogin(String email, String password, Activity activity){
        ApiService service = retrofit.create(ApiService.class);
        Observable<TokenInfo> token = service.getToken(email, password);
        token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> setToken(list.access_token),  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー" + LogUtil.getLog() + throwable.toString());
                            // ログインページへ
                            Util.setToken("");
                            SharedPreferences sharedPref = activity.getSharedPreferences(LoginActivity.PREF_FILE_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(LoginActivity.KEY_EMAIL, "");
                            editor.putString(LoginActivity.KEY_PASSWORD, "");
                            editor.apply();
                            // ログイン画面へ
                            Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                            activity.startActivity(intent);
                        }
                );
    }
}
