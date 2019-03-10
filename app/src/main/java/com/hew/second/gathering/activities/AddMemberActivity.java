package com.hew.second.gathering.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.FriendList;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.AddMemberAdapter;
import com.hew.second.gathering.views.adapters.MemberAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class AddMemberActivity extends BaseActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AddMemberAdapter adapter = null;
    private ArrayList<Friend> ar = new ArrayList<>();
    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);
        setTitle("友達申請");

        // Backボタンを有効にする
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mSwipeRefreshLayout = findViewById(R.id.swipeLayout);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((l) -> new IntentIntegrator(AddMemberActivity.this).initiateScan());

        listView = findViewById(R.id.member_list);
        listView.setEmptyView(findViewById(R.id.emptyView_add_member));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            dialog = new SpotsDialog.Builder().setContext(this).build();
            dialog.show();
            ApiService service = Util.getService();
            HashMap<String, String> body = new HashMap<>();
            body.put("email", adapter.getList().get(position).email);
            Observable<Friend> token = service.requestAddFriend(body);
            cd.add(token.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe(
                            (list) -> {
                                dialog.dismiss();
                                fetchList();
                                final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "友達申請を送信しました。", Snackbar.LENGTH_SHORT);
                                snackbar.getView().setBackgroundColor(Color.BLACK);
                                snackbar.setActionTextColor(Color.WHITE);
                                snackbar.show();
                            }, // 終了時
                            (throwable) -> {
                                Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                                dialog.dismiss();
                                if (!cd.isDisposed()) {
                                    if (throwable instanceof HttpException && ((HttpException) throwable).code() == 409) {
                                        //JSONObject jObjError = new JSONObject(((HttpException)throwable).response().errorBody().string());
                                        final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "すでに友達か、申請中です。", Snackbar.LENGTH_LONG);
                                        snackbar.getView().setBackgroundColor(Color.BLACK);
                                        snackbar.setActionTextColor(Color.WHITE);
                                        snackbar.show();
                                    } else if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                        Intent intent = new Intent(getApplication(), LoginActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }));
        });

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnClickListener((v) -> {
            searchView.setIconified(false);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                List<Friend> filteredItems;
                // フィルター処理
                if (s.isEmpty()) {
                    filteredItems = new ArrayList<>(ar);
                } else {
                    filteredItems = new ArrayList<>();
                    for (Friend item : ar) {
                        if (item.unique_id.toLowerCase().contains(s.toLowerCase()) || item.username.toLowerCase().contains(s.toLowerCase())) { // テキストがqueryを含めば検索にHITさせる
                            filteredItems.add(item);
                        }
                    }
                }
                // adapterの更新処理
                updateList(filteredItems);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                List<Friend> filteredItems;
                // フィルター処理
                if (s.isEmpty()) {
                    filteredItems = new ArrayList<>(ar);
                } else {
                    filteredItems = new ArrayList<>();
                    for (Friend item : ar) {
                        if (item.unique_id.toLowerCase().contains(s.toLowerCase()) || item.username.toLowerCase().contains(s.toLowerCase())) { // テキストがqueryを含めば検索にHITさせる
                            filteredItems.add(item);
                        }
                    }
                }
                // adapterの更新処理
                updateList(filteredItems);
                return true;
            }
        });
        fetchList();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.clearFocus();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            Log.d("readQR", result.getContents());
            SearchView searchView = findViewById(R.id.searchView);
            searchView.setQuery(result.getContents(),true);
            searchView.setFocusable(true);
            searchView.setIconified(false);
            searchView.requestFocusFromTouch();

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void fetchList() {
        mSwipeRefreshLayout.setRefreshing(true);
        ApiService service = Util.getService();
        Observable<FriendList> token = service.getAddableFriendList();
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            ar = new ArrayList<>(list.data);
                            updateList(ar);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            mSwipeRefreshLayout.setRefreshing(false);
                            // ログインアクティビティへ遷移
                            if (!cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                ));
    }

    private void updateList(List<Friend> data) {
        // ListView生成
        ArrayList<Friend> list = new ArrayList<>(data);
        if (!list.isEmpty()) {
            list.add(null);
        }
        // 検索用リスト
        adapter = new AddMemberAdapter(list);
        if(listView != null){
            // ListViewにadapterをセット
            listView.setAdapter(adapter);
        }
    }

}
