package com.example.eventer.member.activities;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.MotionEvent;

import com.example.eventer.member.fragments.LoginFragment;
import com.example.eventer.member.R;
import com.example.eventer.member.api.Util;

public class LoginActivity extends BaseActivity {
    public static final String KEY_EMAIL = "EMAIL";
    public static final String KEY_PASSWORD = "PASSWORD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("ログイン");
        Util.setLoading(true,this);

        if (savedInstanceState == null) {
            // ログインページへ
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container, LoginFragment.newInstance(""));
            fragmentTransaction.commit();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!Util.isLoading()) {
            super.dispatchTouchEvent(ev);
        }
        return Util.isLoading();
    }

    @Override
    public void onBackPressed() {
        // ログイン画面では戻れない
    }
}
