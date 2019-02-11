package com.hew.second.gathering.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hew.second.gathering.R;

public class SignUpActivity extends AppCompatActivity {

    LinearLayout mLoginContainer;
    AnimationDrawable mAnimationDrawable;

    EditText email_et, username_et, password_et, password_confirm_et;
    Button sign_up_btn;
    TextView go_to_login_btn;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mLoginContainer = (LinearLayout) findViewById(R.id.login_container);
        mAnimationDrawable = (AnimationDrawable) mLoginContainer.getBackground();
        mAnimationDrawable.setEnterFadeDuration(5000);
        mAnimationDrawable.setExitFadeDuration(2000);

        email_et = (EditText) findViewById(R.id.user_email);
        username_et = (EditText) findViewById(R.id.user_name);
        password_et = (EditText) findViewById(R.id.user_password);
        password_confirm_et = (EditText) findViewById(R.id.user_password_confirm);
        sign_up_btn = (Button) findViewById(R.id.sign_up_btn);
        go_to_login_btn = (TextView) findViewById(R.id.go_to_login_btn);
        mProgressDialog = new ProgressDialog(this);

        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        go_to_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            finish();
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            }
        });
    }

    private void register() {

        mProgressDialog.setTitle("ログイン");
        mProgressDialog.setMessage("少々お待ちください");
        mProgressDialog.show();

        final String email = email_et.getText().toString();
        final String username = username_et.getText().toString();
        final String password = password_et.getText().toString();
        String password_confirm = password_confirm_et.getText().toString();

        if (!email.contains("@")) {
            email_et.setError("形式が正しくありません");
            email_et.requestFocus();
            mProgressDialog.dismiss();
            return;
        }
        if (TextUtils.isEmpty(username)) {
            username_et.setError("ユーザーネームを入力してください");
            username_et.requestFocus();
            mProgressDialog.dismiss();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            password_et.setError("パスワードを入力してください");
            password_et.requestFocus();
            mProgressDialog.dismiss();
            return;
        }

        if (TextUtils.isEmpty(password_confirm)) {
            password_confirm_et.setError("パスワード(確認用)を入力してください");
            password_confirm_et.requestFocus();
            mProgressDialog.dismiss();
            return;
        }

        if (!password.equals(password_confirm)) {
            password_et.setError("パスワードが一致しません");
            password_et.requestFocus();
            mProgressDialog.dismiss();
            return;
        }

        // TODO:registerAPIを叩いて、sharedPreferenceに格納する
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

        // TODO:ログインしてたらry
    }
}
