package com.hew.second.gathering.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.FriendList;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.MemberAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class AddMemberActivity extends BaseActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MemberAdapter adapter = null;
    private ArrayList<Friend> ar = new ArrayList<>();
    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_member);

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

        listView = findViewById(R.id.member_list);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ApiService service = Util.getService();
            HashMap<String, String> body = new HashMap<>();
            body.put("email", adapter.getList().get(position).email);
            Observable<Friend> token = service.requestAddFriend(LoginUser.getToken(), body);
            cd.add(token.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe(
                            (list) -> {
                                fetchList();
                                final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "友達申請を送信しました。", Snackbar.LENGTH_SHORT);
                                snackbar.getView().setBackgroundColor(Color.BLACK);
                                snackbar.setActionTextColor(Color.WHITE);
                                snackbar.show();
                            }, // 終了時
                            (throwable) -> {
                                Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());

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
                searchView.clearFocus();
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
                adapter.clear();
                adapter.addAll(filteredItems);
                adapter.notifyDataSetChanged();
                return true;
            }
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
        fetchList();
    }
    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }
    private void fetchList() {
        mSwipeRefreshLayout.setRefreshing(true);
        ApiService service = Util.getService();
        Observable<FriendList> token = service.getAddableFriendList(LoginUser.getToken());
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            updateList(list.data);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
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
        // 検索用リスト
        ar = new ArrayList<>(list);
        adapter = new MemberAdapter(list);
        if(listView != null){
            // ListViewにadapterをセット
            listView.setAdapter(adapter);
        }
    }

}
