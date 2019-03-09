package com.hew.second.gathering.api;

import android.content.Context;
import android.graphics.Color;
import android.net.Proxy;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenRefreshAuthenticator implements Authenticator {

    private Observable<JWT> login = LoginUser.getService()
            .getToken(LoginUser.getEmail(Util.getSharedPref()), LoginUser.getPassword(Util.getSharedPref()))
            .publish().refCount();

    @Override
    public Request authenticate(Route route, Response response) throws IOException {

        // Authorizationヘッダーがない（ログイン時）は一回で終了
        if (response.request().header("Authorization") == null) {
            return null; // Give up, we've already failed to authenticate.
        }

        if (response.code() == 401 && responseCount(response) < 3) {
            JWT jwt = login.blockingSingle();
            LoginUser.setToken(jwt.access_token);
            Log.d("api", LoginUser.getToken());
            return response.request()
                    .newBuilder()
                    .header("Authorization", LoginUser.getToken())
                    .build();
        }

        return null;

    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
