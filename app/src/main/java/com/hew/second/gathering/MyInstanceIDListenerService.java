package com.hew.second.gathering;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = MyInstanceIDListenerService.class.getSimpleName();

    @Override
    public void onTokenRefresh() { // トークンが更新されたときに呼び出される

        // deviceTokenの取得
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + deviceToken);
    }
}
