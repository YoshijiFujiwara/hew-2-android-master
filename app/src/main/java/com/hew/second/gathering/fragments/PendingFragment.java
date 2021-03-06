package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.AddMemberActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.activities.MainActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.FriendList;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.MemberAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class PendingFragment extends BaseFragment {
    private static final String MESSAGE = "message";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MemberAdapter adapter = null;
    private ArrayList<Friend> ar = new ArrayList<>();
    private ListView listView = null;

    public static PendingFragment newInstance(int kind) {
        PendingFragment fragment = new PendingFragment();

        Bundle args = new Bundle();
        args.putInt(MESSAGE, kind);
        fragment.setArguments(args);

        return fragment;
    }

    public static PendingFragment newInstance() {
        return new PendingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_pending, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout_pending);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        listView = activity.findViewById(R.id.member_list_pending);
        listView.setEmptyView(activity.findViewById(R.id.emptyView_pending));
        // 申請受諾
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if(adapter != null){
                if (view.getId() == R.id.member_delete) {
                    new MaterialDialog.Builder(activity)
                            .title("友達申請")
                            .content(adapter.getList().get(position).username + "さんからの友達申請を断りますか？")
                            .positiveText("OK")
                            .onPositive((dialog, which) -> {
                                rejectFriend(adapter.getList().get(position).id);
                            })
                            .negativeText("キャンセル")
                            .show();
                } else {
                    new MaterialDialog.Builder(activity)
                            .title("友達申請")
                            .content(adapter.getList().get(position).username + "さんと友達になりますか？")
                            .positiveText("OK")
                            .onPositive((dialog, which) -> {
                                permitFriend(adapter.getList().get(position).id);
                            })
                            .negativeText("キャンセル")
                            .show();
                }
            }
        });
        // 申請拒否

        SearchView searchView = activity.findViewById(R.id.searchView_pending);
        searchView.setOnClickListener((v) -> {
            searchView.setIconified(false);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchView.clearFocus();

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
                ArrayList<Friend> filteredItems;
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
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchList();
    }

    // Resumeの代わり
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void fetchList() {
        mSwipeRefreshLayout.setRefreshing(true);
        ApiService service = Util.getService();
        Observable<FriendList> friendList;
        friendList = service.getPendedFriendList();
        cd.add(friendList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            if (activity != null) {
                                ar = new ArrayList<>(list.data);
                                updateList(list.data);
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            mSwipeRefreshLayout.setRefreshing(false);
                            if (activity != null && !cd.isDisposed()) {
                                if (throwable instanceof NullPointerException){
                                    ar = new ArrayList<>();
                                    updateList(ar);
                                }
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    // ログインアクティビティへ遷移
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                ));
    }

    private void updateList(List<Friend> data) {
        // ListView生成
        listView = activity.findViewById(R.id.member_list_pending);
        if (listView != null) {
            ArrayList<Friend> list = new ArrayList<>(data);
            adapter = new MemberAdapter(list);
            // ListViewにadapterをセット
            listView.setAdapter(adapter);
        }
    }

    private void permitFriend(int id) {
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        ApiService service = Util.getService();
        HashMap<String, Integer> body = new HashMap<>();
        body.put("user_id", id);
        Completable friendList = service.permitFriendRequest(body);
        cd.add(friendList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            if (activity != null) {
                                dialog.dismiss();
                                ((MainActivity)activity).requestUpdateFriend = true;
                                final Snackbar sbYes = Snackbar.make(view, "友達になりました！", Snackbar.LENGTH_SHORT);
                                sbYes.getView().setBackgroundColor(Color.BLACK);
                                sbYes.setActionTextColor(Color.WHITE);
                                sbYes.show();
                                fetchList();
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                dialog.dismiss();
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    // ログインアクティビティへ遷移
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);

                                }
                            }
                        }
                ));
    }

    private void rejectFriend(int id) {
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        ApiService service = Util.getService();
        HashMap<String, Integer> body = new HashMap<>();
        body.put("user_id", id);
        Completable friendList = service.rejectFriendRequest(body);
        cd.add(friendList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            if (activity != null) {
                                dialog.dismiss();
                                final Snackbar sbNo = Snackbar.make(view, "申請を拒否しました。", Snackbar.LENGTH_SHORT);
                                sbNo.getView().setBackgroundColor(Color.BLACK);
                                sbNo.setActionTextColor(Color.WHITE);
                                sbNo.show();
                                fetchList();
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                dialog.dismiss();
                                mSwipeRefreshLayout.setRefreshing(false);
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    // ログインアクティビティへ遷移
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                ));
    }

}
