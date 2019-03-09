package com.hew.second.gathering.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Attribute;
import com.hew.second.gathering.api.AttributeList;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.FriendDetail;
import com.hew.second.gathering.api.FriendList;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.hotpepper.Genre;
import com.hew.second.gathering.hotpepper.GenreResult;
import com.hew.second.gathering.hotpepper.HpHttp;
import com.hew.second.gathering.views.adapters.MemberAdapter;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;
import icepick.State;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class MemberDetailActivity extends BaseActivity {

    private List<Attribute> attrList = new ArrayList<>();
    private Spinner spinner = null;
    @State
    public Friend friend = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_detail);

        friend = Parcels.unwrap(getIntent().getParcelableExtra("FRIEND_DETAIL"));

        TextView name = findViewById(R.id.name);
        name.setText(friend.username);
        TextView uniqueId = findViewById(R.id.unique_id);
        uniqueId.setText(friend.unique_id);
        TextView plusMinus = findViewById(R.id.plus_minus);
        if (friend.attribute != null) {
            plusMinus.setText(friend.attribute.plus_minus == null ? "0" : friend.attribute.plus_minus.toString());
        } else {
            plusMinus.setText("設定されていません。");
        }

        Button save = findViewById(R.id.save);
        save.setOnClickListener((l) -> {
            saveAttribute();
        });

        // Backボタンを有効にする
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getAttributeList();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void getAttributeList() {
        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();
        Observable<AttributeList> attributeList = Util.getService().getAttributeList();
        cd.add(attributeList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            attrList = new ArrayList<>(list.data);
                            ArrayList<String> data = new ArrayList<>();
                            data.add("（なし）");
                            int pos = 0;
                            for (int i = 0; i < attrList.size(); i++) {
                                data.add(attrList.get(i).name);
                                if (friend.attribute != null) {
                                    if (friend.attribute.id.equals(attrList.get(i).id)) {
                                        pos = i + 1;
                                    }
                                }
                            }
                            spinner = findViewById(R.id.attribute);
                            ArrayAdapter adapter =
                                    new ArrayAdapter(this, android.R.layout.simple_spinner_item, data.toArray(new String[0]));
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            if (spinner != null) {
                                spinner.setAdapter(adapter);
                                spinner.setSelection(pos);
                            }
                            if (dialog != null) {
                                dialog.dismiss();
                            }

                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (!cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    // ログインアクティビティへ遷移
                                    Intent intent = new Intent(this.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                        }
                ));
    }

    private void saveAttribute() {
        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();
        String selected = spinner.getSelectedItem().toString();
        Integer attrId = null;
        for (Attribute a : attrList) {
            if (a.name.equals(selected)) {
                attrId = a.id;
            }
        }
        HashMap<String, String> body = new HashMap<>();
        body.put("attribute_id", attrId == null ? null : attrId.toString());
        Observable<FriendDetail> attributeList = Util.getService().updateFriendAttribute(friend.id, body);
        cd.add(attributeList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            Intent intent = new Intent();
                            intent.putExtra(SNACK_MESSAGE, "設定を保存しました。");
                            setResult(RESULT_OK, intent);
                            finish();
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (!cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    // ログインアクティビティへ遷移
                                    Intent intent = new Intent(this.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                        }
                ));
    }

}
