package com.hew.second.gathering.activities;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.FriendList;
import com.hew.second.gathering.api.Group;
import com.hew.second.gathering.api.GroupUserList;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.SessionUserList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.fragments.GroupFragment;
import com.hew.second.gathering.fragments.MemberFragment;
import com.hew.second.gathering.views.adapters.GroupMemberListAdapter;
import com.hew.second.gathering.views.adapters.MemberAdapter;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;
import icepick.State;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class AddGroupMemberActivity extends BaseActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GroupMemberListAdapter adapter = null;
    private ArrayList<Friend> ar = new ArrayList<>();
    private ListView listView = null;
    @State
    public Group group = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_member);
        setTitle("グループに追加");

        // Backボタンを有効にする
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        Intent beforeIntent = getIntent();
        if (beforeIntent != null) {
            Bundle bundle = beforeIntent.getExtras();
            if (bundle != null) {
                group = Parcels.unwrap(getIntent().getParcelableExtra("GROUP_DETAIL"));
            }
        }
        if (group == null) {
            finish();
        }

        mSwipeRefreshLayout = findViewById(R.id.swipeLayout);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        Button addGroupBtn = findViewById(R.id.add_button);
        addGroupBtn.setOnClickListener((l) -> {
            if (adapter.getCheckedCount() <= 0) {
                final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "グループに追加する人を選択してください。", Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(Color.BLACK);
                snackbar.setActionTextColor(Color.WHITE);
                snackbar.show();
                return;
            }
            new MaterialDialog.Builder(this)
                    .title(group.name)
                    .content(adapter.getCheckedCount() + "名をグループに追加しますか？")
                    .positiveText("OK")
                    .onPositive((dialog, which) -> {
                        addGroupMember();
                    })
                    .negativeText("キャンセル")
                    .show();
        });

        listView = findViewById(R.id.member_list);
        listView.setEmptyView(findViewById(R.id.emptyView_add_group_member));
        listView.setOnItemClickListener((parent, view, position, id) -> {

            if (adapter.getChecked(position)) {
                adapter.setChecked(position, false);
            } else {
                adapter.setChecked(position, true);
            }
            adapter.notifyDataSetChanged();
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable("GROUP_DETAIL", Parcels.wrap(group));
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void fetchList() {
        mSwipeRefreshLayout.setRefreshing(true);
        ApiService service = Util.getService();
        Observable<FriendList> token = service.getAddableToGroupFriendList(group.id);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            // 検索用リスト
                            ar = new ArrayList<>(list.data);
                            updateList(list.data);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            mSwipeRefreshLayout.setRefreshing(false);
                            // ログインアクティビティへ遷移
                            if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                Intent intent = new Intent(getApplication(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                ));
    }

    private void updateList(List<Friend> data) {
        // ListView生成
        ArrayList<Friend> list = new ArrayList<>(data);
        adapter = new GroupMemberListAdapter(list);
        if (listView != null) {
            // ListViewにadapterをセット
            listView.setAdapter(adapter);
        }
    }

    private void addGroupMember() {
        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();
        List<Boolean> sba = adapter.getCheckedList();
        List<Friend> work = adapter.getList();
        ApiService service = Util.getService();
        ArrayList<Observable<GroupUserList>> addList = new ArrayList<>();
        for (int i = 0; i < sba.size(); i++) {
            if (sba.get(i)) {
                HashMap<String, Integer> body = new HashMap<>();
                body.put("user_id", work.get(i).id);
                addList.add(service.addUserToGroup(group.id, body));
            }
        }
        cd.add(Observable
                .merge(addList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        (list) -> {
                            group.users = list.data;
                        },
                        // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (!cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                                dialog.dismiss();
                                fetchList();
                            }
                        },
                        () -> {
                            dialog.dismiss();
                            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "グループに追加しました。", Snackbar.LENGTH_SHORT);
                            snackbar.getView().setBackgroundColor(Color.BLACK);
                            snackbar.setActionTextColor(Color.WHITE);
                            snackbar.show();
                            fetchList();
                        }
                ));
    }

}