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
import io.reactivex.schedulers.Schedulers;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenRefreshAuthenticator implements Authenticator {
    @Override
    public Request authenticate(Route route, Response response) throws IOException {

        // TODO error handling for unrecoverable situation
        Observable<JWT> token = Util.getService().getToken(LoginUser.getEmail(null), LoginUser.getPassword(null));
        LoginUser.setToken(token.blockingFirst().access_token);
        return response.request().newBuilder().build();
    }
}
