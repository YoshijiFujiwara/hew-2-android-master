package com.example.eventer.member;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.eventer.member.api.ApiService;
import com.example.eventer.member.api.TokenInfo;
import com.example.eventer.member.api.Util;

import java.lang.reflect.Member;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private Button mLogin;
    Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Backボタンを有効にする
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mLogin = findViewById(R.id.button_login);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLogin(mEmail.getText().toString(),mPassword.getText().toString());
            }
        });
        retrofit = Util.getRetrofit();
    }

    private void checkLogin(String email,String password){
        ApiService service = retrofit.create(ApiService.class);
        Observable<TokenInfo> token = service.getToken(email, password);
        token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> finishLogin(list),  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー" + LogUtil.getLog() + throwable.toString());
                            //TODO:ログインページに戻す エラーメッセージとか表示も
                            Intent intent = new Intent(getApplication(), LoginActivity.class);
                            startActivity(intent);
                        }
                );
    }
    private void finishLogin(TokenInfo token) {
        Util.setToken(token.access_token);
        Intent intent = new Intent(getApplication(), MemberActivity.class);
        startActivity(intent);
    }
}
