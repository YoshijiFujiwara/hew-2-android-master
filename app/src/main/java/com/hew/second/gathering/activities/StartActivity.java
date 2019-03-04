package com.hew.second.gathering.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ImageButton now_button = (ImageButton) findViewById(R.id.now_button);
        ImageButton plan_button = (ImageButton) findViewById(R.id.plan_button);
        now_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                //  今から飲むモードへ
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
