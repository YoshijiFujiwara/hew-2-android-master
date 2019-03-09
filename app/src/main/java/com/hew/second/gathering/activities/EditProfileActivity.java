package com.hew.second.gathering.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Profile;
import com.hew.second.gathering.api.ProfileDetail;
import com.hew.second.gathering.api.Util;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class EditProfileActivity extends BaseActivity {

    private boolean uniqueIdFlag = true;
    private boolean usernameFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        setTitle("アカウント設定");

        // Backボタンを有効にする
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        EditText uniqueId = findViewById(R.id.unique_id);
        uniqueId.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた場合キーボードを非表示にする
                InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        EditText userName = findViewById(R.id.user_name);
        userName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた場合キーボードを非表示にする
                InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        EditText mailAddress = findViewById(R.id.mail_address);
        mailAddress.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた場合キーボードを非表示にする
                InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        Switch uniqueIdSearchFlag = findViewById(R.id.unique_id_search_flag);
        Switch usernameSearchFlag = findViewById(R.id.username_search_flag);

        uniqueIdSearchFlag.setOnCheckedChangeListener((v,b)->{
            uniqueIdFlag = b;
        });
        usernameSearchFlag.setOnCheckedChangeListener((v,b)->{
            usernameFlag = b;
        });

        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();

        ApiService service = Util.getService();
        Observable<ProfileDetail> profile = service.getProfile();
        cd.add(profile.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            dialog.dismiss();
                            profile(list.data);
                        },  // 成功時
                        throwable -> {
                            dialog.dismiss();
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                Intent intent = new Intent(getApplication(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                ));

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // データ保存Loadin
                saveProfile();
            }
        });


    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        removeFocus();
        return super.dispatchTouchEvent(ev);
    }

    public void removeFocus() {
        InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodMgr.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void profile(Profile data) {
        EditText uniqueId = findViewById(R.id.unique_id);
        EditText userName = findViewById(R.id.user_name);
        EditText mailAddress = findViewById(R.id.mail_address);
        Switch uniqueIdSearchFlag = findViewById(R.id.unique_id_search_flag);
        Switch usernameSearchFlag = findViewById(R.id.username_search_flag);

        uniqueId.setText(data.unique_id);
        userName.setText(data.username);
        mailAddress.setText(data.email);

        if (data.unique_id_search_flag == 1) {
            uniqueIdFlag = true;
        } else {
            uniqueIdFlag = false;
        }
        uniqueIdSearchFlag.setChecked(uniqueIdFlag);

        if (data.username_search_flag == 1) {
            usernameFlag = true;
        } else {
            usernameFlag = false;
        }
        usernameSearchFlag.setChecked(usernameFlag);
    }

    public void saveProfile() {
        ApiService service = Util.getService();

        EditText uniqueId = findViewById(R.id.unique_id);
        EditText userName = findViewById(R.id.user_name);
        EditText mailAddress = findViewById(R.id.mail_address);
        Switch uniqueIdSearchFlag = findViewById(R.id.unique_id_search_flag);
        Switch usernameSearchFlag = findViewById(R.id.username_search_flag);

        String uid = uniqueId.getText().toString();
        if (TextUtils.isEmpty(uid)) {
            uniqueId.setError("IDを入力してください。");
            uniqueId.requestFocus();
            return;
        }
        String strUsername = userName.getText().toString();
        if (TextUtils.isEmpty(strUsername)) {
            userName.setError("ユーザー名を入力してください。");
            userName.requestFocus();
            return;
        }
        String strEmail = mailAddress.getText().toString();
        if (TextUtils.isEmpty(strEmail)) {
            mailAddress.setError("Eメールを入力してください。");
            mailAddress.requestFocus();
            return;
        }

        HashMap<String, String> body = new HashMap<>();

        body.put("unique_id", uid);
        body.put("username", strUsername);
        body.put("email", strEmail);
        body.put("unique_id_search_flag", uniqueIdFlag ? "1" : "0");
        body.put("username_search_flag", usernameFlag ? "1" : "0");
        String password = (LoginUser.getPassword(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)));
        body.put("password", password);

        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();

        Observable<ProfileDetail> token = service.updateProfileUser(body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            dialog.dismiss();

                            LoginUser.setEmail(getSharedPreferences(Util.PREF_FILE_NAME, MODE_PRIVATE), list.data.email);
                            LoginUser.setUniqueId(list.data.unique_id);
                            LoginUser.setUsername(list.data.username);

                            Intent intent = new Intent();
//                                Intent intent = new Intent(activity.getApplication(), MainActivity.class);
                            intent.putExtra(SNACK_MESSAGE, "アカウントを更新しました。");
                            setResult(RESULT_OK, intent);
                            finish();
                        },  // 成功時
                        throwable -> {
                            dialog.dismiss();
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (!cd.isDisposed()) {
                                if (throwable instanceof HttpException && ((HttpException) throwable).code() == 409) {
                                    Intent intent = new Intent();
                                    intent.putExtra(SNACK_MESSAGE, "アカウントの変更はありません。");
                                    setResult(RESULT_CANCELED, intent);
                                    finish();
                                } else if (throwable instanceof HttpException && ((HttpException) throwable).code() == 422) {
                                    final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "IDまたはEメールが重複しています。", Snackbar.LENGTH_SHORT);
                                    snackbar.getView().setBackgroundColor(Color.BLACK);
                                    snackbar.setActionTextColor(Color.WHITE);
                                    snackbar.show();
                                } else if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent2 = new Intent(getApplication(), LoginActivity.class);
                                    startActivity(intent2);
                                }
                            }
                        }
                ));
    }
}
