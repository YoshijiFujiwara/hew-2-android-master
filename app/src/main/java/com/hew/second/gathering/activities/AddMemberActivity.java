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
import android.widget.Button;
import android.widget.ListView;
import android.widget.ToggleButton;

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
    private ToggleButton searchArgs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);
        setTitle("友達申請");

        // Backボタンを有効にする
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mSwipeRefreshLayout = findViewById(R.id.swipeLayout);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> mSwipeRefreshLayout.setRefreshing(false));


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((l) -> {
            Intent intent = new Intent(getApplication(), ShowQrCodeActivity.class);
            startActivity(intent);
        });

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

        searchArgs = findViewById(R.id.toggleButton_search_args);
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnClickListener((v) -> {
            searchView.setIconified(false);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // 検索API
                if(s.isEmpty()){
                    final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "検索ワードを入力してください。", Snackbar.LENGTH_SHORT);
                    snackbar.getView().setBackgroundColor(Color.BLACK);
                    snackbar.setActionTextColor(Color.WHITE);
                    snackbar.show();
                    return false;
                }
                searchArgs = findViewById(R.id.toggleButton_search_args);
                searchFriend(s,searchArgs.isChecked());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });

        Button search = findViewById(R.id.search_button_add_member);
        search.setOnClickListener((l) -> {
            if(searchView.getQuery().toString().isEmpty()){
                final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "検索ワードを入力してください。", Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(Color.BLACK);
                snackbar.setActionTextColor(Color.WHITE);
                snackbar.show();
                return;
            }
            // 検索API
            searchArgs = findViewById(R.id.toggleButton_search_args);
            searchFriend(searchView.getQuery().toString(),searchArgs.isChecked());
        });


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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void searchFriend(String param, boolean isId) {
        mSwipeRefreshLayout.setRefreshing(true);
        ApiService service = Util.getService();
        Observable<FriendList> friendList;
        HashMap<String, String> body = new HashMap<>();
        if (isId) {
            body.put("unique_id",param);
            friendList = service.searchUserByUniqueId(body);
        } else {
            body.put("username",param);
            friendList = service.searchUserByUsername(body);
        }
        cd.add(friendList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            ar = new ArrayList<>(list.data);
                            updateList(ar);
                            mSwipeRefreshLayout.setRefreshing(false);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());

                            // ログインアクティビティへ遷移
                            if (!cd.isDisposed()) {
                                if (throwable instanceof NullPointerException){
                                    ar = new ArrayList<>();
                                    updateList(ar);
                                }
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        }
                ));
    }

    private void updateList(List<Friend> data) {
        // ListView生成
        ArrayList<Friend> list = new ArrayList<>(data);
        if (!list.isEmpty()) {
            list.add(null);
        } else {
            mSwipeRefreshLayout = findViewById(R.id.swipeLayout);
            mSwipeRefreshLayout.setRefreshing(false);
        }
        // 検索用リスト
        adapter = new AddMemberAdapter(list);
        if (listView != null) {
            // ListViewにadapterをセット
            listView.setAdapter(adapter);
        }
    }

}
