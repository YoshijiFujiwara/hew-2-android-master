package com.hew.second.gathering.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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

public class SignUpActivity extends BaseActivity {

    LinearLayout mLoginContainer;
    AnimationDrawable mAnimationDrawable;

    EditText email_et, username_et, password_et, password_confirm_et;
    Button sign_up_btn;
    TextView go_to_login_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mLoginContainer = findViewById(R.id.login_container);
        mAnimationDrawable = (AnimationDrawable) mLoginContainer.getBackground();
        mAnimationDrawable.setEnterFadeDuration(5000);
        mAnimationDrawable.setExitFadeDuration(2000);

        email_et = findViewById(R.id.user_email);
        username_et = findViewById(R.id.user_name);
        password_et = findViewById(R.id.user_password);
        password_confirm_et = findViewById(R.id.user_password_confirm);
        sign_up_btn = findViewById(R.id.sign_up_btn);
        go_to_login_btn = findViewById(R.id.go_to_login_btn);

        sign_up_btn.setOnClickListener((v) -> register());

        go_to_login_btn.setOnClickListener((v) -> {
            onBackPressed();
        });
    }

    private void register() {

        final String email = email_et.getText().toString();
        final String username = username_et.getText().toString();
        final String password = password_et.getText().toString();
        String password_confirm = password_confirm_et.getText().toString();

        if (!email.contains("@")) {
            email_et.setError("形式が正しくありません");
            email_et.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(username)) {
            username_et.setError("ユーザーネームを入力してください");
            username_et.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            password_et.setError("パスワードを入力してください");
            password_et.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password_confirm)) {
            password_confirm_et.setError("パスワード(確認用)を入力してください");
            password_confirm_et.requestFocus();
            return;
        }

        if (!password.equals(password_confirm)) {
            password_et.setError("パスワードが一致しません");
            password_et.requestFocus();
            return;
        }

        // sharedPreferenceに格納する
        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();

        LoginApiService service = LoginUser.getService();
        Observable<JWT> token = service.createUser(email, username, password);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            dialog.dismiss();
                            finishCreate(list);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー" + LogUtil.getLog() + throwable.toString());
                            dialog.dismiss();
                            // ログイン情報初期化
                            LoginUser.deleteUserInfo(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE));
                            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "登録に失敗しました。入力内容をご確認ください。", Snackbar.LENGTH_SHORT);
                            snackbar.getView().setBackgroundColor(Color.BLACK);
                            snackbar.setActionTextColor(Color.WHITE);
                            snackbar.show();
                        }
                ));
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
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodMgr.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void finishCreate(JWT token) {
        // ログイン情報を保存
        LoginUser.setUserInfo(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE),
                email_et.getText().toString(), password_et.getText().toString(), token.access_token);
        // androidデバイストークン送信
        sendTokenToServer();
        // TOP画面へ
        Intent intent = new Intent(getApplication(), StartActivity.class);
        intent.putExtra(SNACK_MESSAGE, "アカウントを作成しました。");
        startActivityForResult(intent, INTENT_LOGIN);
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
