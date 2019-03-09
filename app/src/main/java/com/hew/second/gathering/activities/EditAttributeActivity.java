package com.hew.second.gathering.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Attribute;
import com.hew.second.gathering.api.AttributeDetail;
import com.hew.second.gathering.api.Util;

import org.parceler.Parcels;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class EditAttributeActivity extends BaseActivity {

    private EditText name;
    private EditText plus_minus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_attribute);
        setTitle("属性設定");

        // Backボタンを有効にする
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        Attribute attribute = Parcels.unwrap(getIntent().getParcelableExtra("ATTRIBUTE_DETAIL"));

        name = findViewById(R.id.attribute_name);
        plus_minus = findViewById(R.id.attribute_plus_minus);
        Button save = findViewById(R.id.attribute_save);

        save.setOnClickListener((l) -> {
            saveAttribute(attribute);
        });

        if (attribute != null) {
            name.setText(attribute.name);
            plus_minus.setText(attribute.plus_minus.toString());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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

    private void saveAttribute(Attribute attribute) {

        ApiService service = Util.getService();
        Observable<AttributeDetail> attr;
        HashMap<String, String> body = new HashMap<>();
        name = findViewById(R.id.attribute_name);
        plus_minus = findViewById(R.id.attribute_plus_minus);

        String strName = name.getText().toString();
        String strPlusMinus = plus_minus.getText().toString();

        if (TextUtils.isEmpty(strName)) {
            name.setError("属性名を入力してください。");
            name.requestFocus();
            return;
        }

        try {
            int num = Integer.parseInt(strPlusMinus);
            if (TextUtils.isEmpty(strPlusMinus)) {
                plus_minus.setError("加減額を入力してください。");
                plus_minus.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            plus_minus.setError("加減額は数値で入力してください。");
            plus_minus.requestFocus();
            return;
        }

        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();

        body.put("name", strName);
        body.put("plus_minus", strPlusMinus);
        if (attribute != null) {
            attr = service.updateAttribute(attribute.id, body);
        } else {
            attr = service.createAttribute(body);
        }
        cd.add(attr
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            dialog.dismiss();
                            Intent intent = new Intent();
                            intent.putExtra(SNACK_MESSAGE, "属性を更新しました。");
                            setResult(RESULT_OK, intent);
                            finish();
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (!cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                                dialog.dismiss();
                            }
                        }
                ));

    }

}
