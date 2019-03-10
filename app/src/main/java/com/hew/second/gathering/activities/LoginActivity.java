package com.hew.second.gathering.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginApiService;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.DeviceTokenDetail;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class LoginActivity extends BaseActivity {

    LinearLayout mLoginContainer;
    AnimationDrawable mAnimationDrawable;

    EditText email_et, password_et;
    Button login_btn;
    TextView sign_up_btn, forgot_pass_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginContainer = findViewById(R.id.login_container);
        mAnimationDrawable = (AnimationDrawable) mLoginContainer.getBackground();
        mAnimationDrawable.setEnterFadeDuration(5000);
        mAnimationDrawable.setExitFadeDuration(2000);

        email_et = findViewById(R.id.user_email);
        password_et = findViewById(R.id.user_password);
        sign_up_btn = findViewById(R.id.sign_up_btn);
        login_btn = findViewById(R.id.login_btn);
        forgot_pass_btn = findViewById(R.id.forgot_pass_btn);

        login_btn.setOnClickListener((v) -> {
            checkLogin(email_et.getText().toString(), password_et.getText().toString());
        });

        sign_up_btn.setOnClickListener((v) -> {
            // signUpActivityに遷移
            Intent signUpIntent = new Intent(getApplication(), SignUpActivity.class);
            startActivity(signUpIntent);
        });

        forgot_pass_btn.setOnClickListener((v) -> {
            // これから書く？？？forgotPasswordActivity必要になるけど、優先度は低いかもね
        });


        // ログイン情報があるなら格納
        if (!LoginUser.getEmail(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)).equals("")) {
            email_et.setText(LoginUser.getEmail(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)));
            password_et.setText(LoginUser.getPassword(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)));
            LoginUser.deleteUserInfo(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE));
            //checkLogin(LoginUser.getEmail(null), LoginUser.getPassword(null));
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mAnimationDrawable != null && !mAnimationDrawable.isRunning()) {
            mAnimationDrawable.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAnimationDrawable != null && mAnimationDrawable.isRunning()) {
            mAnimationDrawable.stop();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodMgr.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        // ログイン画面で戻ったらアプリ終了
        moveTaskToBack(true);
    }


    private void checkLogin(String email, String password) {
        if (!email.contains("@")) {
            LoginUser.deleteUserInfo(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE));
            email_et.setError("形式が正しくありません");
            email_et.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            LoginUser.deleteUserInfo(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE));
            password_et.setError("パスワードが入力がされていません");
            password_et.requestFocus();
            return;
        }

        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();

        LoginApiService service = LoginUser.getService();
        Observable<JWT> token = service.getToken(email, password);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            dialog.dismiss();
                            finishLogin(list);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー" + LogUtil.getLog() + throwable.toString());
                            dialog.dismiss();
                            // ログイン情報初期化
                            LoginUser.deleteUserInfo(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE));
                            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "ログイン情報が異なります。再度お試しください。", Snackbar.LENGTH_SHORT);
                            snackbar.getView().setBackgroundColor(Color.BLACK);
                            snackbar.setActionTextColor(Color.WHITE);
                            snackbar.show();
                        }
                ));
    }

    private void finishLogin(JWT token) {
        // ログイン情報を保存
        LoginUser.setUserInfo(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE),
                email_et.getText().toString(), password_et.getText().toString(), token.access_token);
        // androidデバイストークン送信
        sendTokenToServer();
        // TOP画面へ
        Intent intent = new Intent(getApplication(), StartActivity.class);
        intent.putExtra(SNACK_MESSAGE, "ログインしました。");
        startActivityForResult(intent, INTENT_LOGIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_LOGIN) {
            if (!LoginUser.getEmail(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)).equals("")) {
                email_et.setText(LoginUser.getEmail(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)));
                password_et.setText(LoginUser.getPassword(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)));
            }
        }
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

