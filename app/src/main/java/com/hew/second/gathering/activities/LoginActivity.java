package com.hew.second.gathering.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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
        // ログイン情報があるならすぐに遷移
        if (!LoginUser.getEmail(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)).equals("")) {
            email_et.setText(LoginUser.getEmail(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)));
            password_et.setText(LoginUser.getPassword(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)));
            checkLogin(LoginUser.getEmail(null), LoginUser.getPassword(null));
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodMgr.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        // ログイン画面では戻れない
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

        ApiService service = Util.getService();
        Observable<JWT> token = service.getToken(email, password);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            finishLogin(list);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー" + LogUtil.getLog() + throwable.toString());
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
        // TOP画面へ
        Intent intent = new Intent(getApplication(), StartActivity.class);
        intent.putExtra(SNACK_MESSAGE, "ログインに成功しました。");
        startActivity(intent);
    }
}

