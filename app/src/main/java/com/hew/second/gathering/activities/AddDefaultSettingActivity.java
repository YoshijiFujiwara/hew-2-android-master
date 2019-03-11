package com.hew.second.gathering.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.DefaultSetting;
import com.hew.second.gathering.api.DefaultSettingDetail;
import com.hew.second.gathering.api.Group;
import com.hew.second.gathering.api.GroupList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.GroupAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class AddDefaultSettingActivity extends BaseActivity {

    int defaultSettingId = -1;
    GroupAdapter adapter = null;
    private List<Group> groupList = new ArrayList<>();
    private Spinner spinner = null;
    private String lat = null;
    private String lng = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_add_default);
        setTitle("テンプレート追加");

        // Backボタンを有効にする
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        EditText defaultName = findViewById(R.id.default_input);
        defaultName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた場合キーボードを非表示にする
                InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        EditText startTime = findViewById(R.id.start_time);
        startTime.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた場合キーボードを非表示にする
                InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        RadioGroup mRadioGroup = findViewById(R.id.RadioGroup);

        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();

        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        Observable<GroupList> token = service.getGroupList();
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            dialog.dismiss();
                            // グループ所持判定
                            if(list.data.isEmpty()){
                                Intent intent = new Intent();
                                intent.putExtra(SNACK_MESSAGE, "先にグループを作成してください。");
                                setResult(RESULT_OK, intent);
                                finish();
                            }else{
                                groupList = new ArrayList<>(list.data);
                                ArrayList<String> data = new ArrayList<>();
                                for (Group g : groupList) {
                                    data.add(g.name);
                                }
                                spinner = findViewById(R.id.group_spinner);
                                ArrayAdapter adapter =
                                        new ArrayAdapter(this, android.R.layout.simple_spinner_item, data.toArray(new String[0]));
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            dialog.dismiss();
                            if (!cd.isDisposed() && throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
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
                saveDefaultSettingName();
            }
        });

        RadioButton rb = findViewById(R.id.specific_location);
        rb.setOnClickListener((l) -> {
            Intent intent = new Intent(getApplication(), DefaultMapActivity.class);
            if (lat != null && lng != null) {
                intent.putExtra("lat", lat);
                intent.putExtra("lng", lng);
            }
            startActivityForResult(intent, INTENT_DEFAULT_MAP);
        });


    }


    public void removeFocus() {
        InputMethodManager inputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodMgr.hideSoftInputFromWindow(findViewById(android.R.id.content).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        removeFocus();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void saveDefaultSettingName() {

        EditText defaultName = findViewById(R.id.default_input);
        EditText startTime = findViewById(R.id.start_time);
        Spinner spinner = findViewById(R.id.group_spinner);
        RadioGroup mRadioGroup = findViewById(R.id.RadioGroup);

        if (TextUtils.isEmpty(defaultName.getText().toString())) {
            defaultName.setError("テンプレート名を入力してください。");
            defaultName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(startTime.getText().toString())) {
            startTime.setError("開始時刻を入力してください。");
            startTime.requestFocus();
            return;
        }

        String strTimer = startTime.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("dd:HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        long date = 0;
        long now = new Date().getTime();
        try {
            date = sdf.parse(strTimer).getTime();
        } catch (Exception e) {
            try {
                int timer = Integer.parseInt(strTimer);
                Integer minute = timer % 60;
                timer /= 60;
                Integer hour = timer % 24;
                timer /= 24;
                Integer day = timer % 30;
                if (day > 30) {
                    throw (new Exception());
                }
                strTimer = String.format("%02d:%02d:%02d", day, hour, minute);

            } catch (Exception ex) {
                startTime.setError("開始時刻の形式が異なります。");
                startTime.requestFocus();
                return;
            }
        }

        int checkedId = mRadioGroup.getCheckedRadioButtonId();
        String flag = "1";
        String latitude = "";
        String longitude = "";
        switch (checkedId) {
            case R.id.current_location:
                flag = "1";
                latitude = "";
                longitude = "";
                break;
            case R.id.specific_location:
                if (lat != null && lng != null) {
                    flag = "0";
                    latitude = lat;
                    longitude = lng;
                }
                break;
        }

        HashMap<String, String> body = new HashMap<>();
        body.put("name", defaultName.getText().toString());
        body.put("timer", strTimer);
        body.put("group_id", String.valueOf(groupList.get((int) spinner.getSelectedItemPosition()).id));
        body.put("current_location_flag", flag);
        body.put("latitude", latitude);
        body.put("longitude", longitude);

        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();
        ApiService service = Util.getService();
        Observable<DefaultSettingDetail> token = service.createDefaultSetting(body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            dialog.dismiss();
                            Intent intent = new Intent();
                            intent.putExtra(SNACK_MESSAGE, "テンプレートを作成しました。");
                            setResult(RESULT_OK, intent);
                            finish();
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            dialog.dismiss();
                            if (!cd.isDisposed()) {
                                Intent intent = new Intent();
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent2 = new Intent(getApplication(), LoginActivity.class);
                                    startActivity(intent2);
                                }
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }
                ));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if (data != null) {
                this.lat = data.getStringExtra("lat");
                this.lng = data.getStringExtra("lng");
            }
        }
        if(lat == null || lng == null){
            RadioButton rb = findViewById(R.id.current_location);
            rb.toggle();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
