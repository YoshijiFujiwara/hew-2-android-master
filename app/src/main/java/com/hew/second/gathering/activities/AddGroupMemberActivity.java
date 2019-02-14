package com.hew.second.gathering.activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.fragments.GroupFragment;
import com.hew.second.gathering.fragments.MemberFragment;
import com.hew.second.gathering.views.adapters.MemberAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AddGroupMemberActivity extends BaseActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MemberAdapter adapter = null;
    private ArrayList<Friend> ar = new ArrayList<>();
    private ListView list = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_member);
        mSwipeRefreshLayout = findViewById(R.id.swipeLayout);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        ListView listView = findViewById(R.id.member_list);
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnClickListener((v) -> {
            searchView.setIconified(false);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // 送信
                // focusout
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                final List<Friend> filteredItems = new ArrayList<Friend>();
                // フィルター処理
                for (Friend item : ar) {
                    if (item.unique_id.contains(s) || item.username.contains(s)) { // テキストがqueryを含めば検索にHITさせる
                        filteredItems.add(item);
                    }
                }
                // adapterの更新処理
                adapter.clear();
                adapter.addAll(filteredItems);
                adapter.notifyDataSetChanged();
                return true;
            }
        });
        // フォーカスを失った時
        /*
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                searchView.clearFocus();
            }
        });
        */
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
        Util.setLoading(true, this);
        fetchList();
    }

    private void fetchList() {
        ApiService service = Util.getService();
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        HashMap<String, String> body = new HashMap<>();
        body.put("username", "test");
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    return service.searchAddableFriendList(LoginUser.getToken(),body);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Util.setLoading(false, this);
                            mSwipeRefreshLayout.setRefreshing(false);
                            updateList(list.data);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            Util.setLoading(false, this);
                            // ログインアクティビティへ遷移
                            Intent intent = new Intent(getApplication(), LoginActivity.class);
                            startActivity(intent);
                        }
                );
    }

    private void updateList(List<Friend> data) {
        // ListView生成
        ListView listView = findViewById(R.id.member_list);
        ar = new ArrayList<>();
        for (Friend m : data) {
            ar.add(m);
        }
        adapter = new MemberAdapter(ar);
        // ListViewにadapterをセット
        listView.setAdapter(adapter);
    }

}
