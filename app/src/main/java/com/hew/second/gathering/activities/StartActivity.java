package com.hew.second.gathering.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.DeviceTokenDetail;
import com.hew.second.gathering.api.Util;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (LoginUser.getEmail(getSharedPreferences(Util.PREF_FILE_NAME, MODE_PRIVATE)).isEmpty()) {
            Intent intent = new Intent(getApplication(), LoginActivity.class);
            startActivity(intent);
        }

        // androidデバイストークン送信
        sendTokenToServer();

        Button now_button = findViewById(R.id.now_button);
        Button plan_button = findViewById(R.id.plan_button);
        now_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 新規イベント作成へ
                plan_button.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        plan_button.setEnabled(true);
                    }
                }, 1000);
                now_button.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        now_button.setEnabled(true);
                    }
                }, 1000);
                // 新規セッション作成へ
                Intent intent = new Intent(getApplication(), EventProcessMainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("FRAGMENT", "DEFAULT");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        plan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plan_button.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        plan_button.setEnabled(true);
                    }
                }, 1000);
                now_button.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        now_button.setEnabled(true);
                    }
                }, 1000);
                //  今から飲むモードへ
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void sendTokenToServer() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("tag", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        postToken(token);
                    }
                });
    }

    private void postToken(String deviceToken) {
        // 取得したデバイストークンを、サーバーに投げる
        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("device_token", deviceToken);
        Observable<DeviceTokenDetail> token = service.storeDeviceToken(body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        (list) -> {
                            Log.d("api", "デバイストークンの送信完了");
                        }, // 終了時
                        (throwable) -> {
                            Log.d("api", "デバイストークンの送信失敗");
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                Intent intent = new Intent(getApplication(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }));
    }
}
