package com.example.eventer.member;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.eventer.member.api.ApiService;
import com.example.eventer.member.api.TokenInfo;
import com.example.eventer.member.api.Util;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

import static com.example.eventer.member.LoginActivity.KEY_EMAIL;
import static com.example.eventer.member.LoginActivity.KEY_PASSWORD;
import static com.example.eventer.member.LoginActivity.PREF_FILE_NAME;

public class LoginFragment extends Fragment {

    private EditText mEmail;
    private EditText mPassword;
    private Button mLogin;
    Retrofit retrofit;

    private static final String MESSAGE = "message";

    public static LoginFragment newInstance(String message) {
        LoginFragment fragment = new LoginFragment();

        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_login,
                container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        retrofit = Util.getRetrofit();
        mEmail = activity.findViewById(R.id.email);
        mPassword = activity.findViewById(R.id.password);
        mLogin = activity.findViewById(R.id.button_login);

        // 設定ファイルを開きます。
        SharedPreferences sharedPref = activity.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        // 値の取得
        String email = sharedPref.getString(KEY_EMAIL, "");
        String password = sharedPref.getString(KEY_PASSWORD, "");
        if(!email.equals("") && !password.equals("")){
            checkLogin(mEmail.getText().toString(),mPassword.getText().toString());
        }

        FloatingActionButton fab = activity.findViewById(R.id.fab_register);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent intent = new Intent(activity.getApplication(), RegisterActivity.class);
                // startActivity(intent);
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkLogin(mEmail.getText().toString(),mPassword.getText().toString());
            }
        });
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
                            // ログイン情報初期化
                            Util.setToken("");
                            SharedPreferences sharedPref = getActivity().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(KEY_EMAIL, "");
                            editor.putString(KEY_PASSWORD, "");
                            editor.apply();

                            final Snackbar snackbar = Snackbar.make(getView(), "ログイン情報が異なります。再度お試しください。", Snackbar.LENGTH_SHORT);
                            snackbar.getView().setBackgroundColor(Color.BLACK);
                            snackbar.setActionTextColor(Color.WHITE);
                            snackbar.show();
                        }
                );
    }
    private void finishLogin(TokenInfo token) {
        // トークンを更新
        Util.setToken(token.access_token);
        // ログイン情報を保存
        SharedPreferences sharedPref = getActivity().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_EMAIL, mEmail.getText().toString());
        editor.putString(KEY_PASSWORD, mPassword.getText().toString());
        editor.apply();
        // TOP画面へ
        Intent intent = new Intent(getActivity().getApplication(), MemberActivity.class);
        startActivity(intent);
    }
}
