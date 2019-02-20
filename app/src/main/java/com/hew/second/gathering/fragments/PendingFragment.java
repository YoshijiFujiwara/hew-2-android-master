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

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.AddMemberActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.FriendList;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.MemberAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        // 申請受諾
        listView.setOnItemClickListener((parent, view, position, id) -> {
            final Snackbar snackbar = Snackbar.make(view, "友達になりますか？", Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(Color.BLACK);
            snackbar.setActionTextColor(Color.WHITE);
            HashMap<String, Integer> body = new HashMap<>();
            body.put("user_id", adapter.getList().get(position).id);
            snackbar.setAction("Yes", (v) -> {
                ApiService service = Util.getService();
                Completable friendList = service.permitFriendRequest(LoginUser.getToken(), body);
                cd.add(friendList.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .unsubscribeOn(Schedulers.io())
                        .subscribe(
                                () -> {
                                    if( activity != null ){
                                        final Snackbar sbYes = Snackbar.make(view, "友達になりました！", Snackbar.LENGTH_SHORT);
                                        sbYes.getView().setBackgroundColor(Color.BLACK);
                                        sbYes.setActionTextColor(Color.WHITE);
                                        sbYes.show();
                                    }
                                },  // 成功時
                                throwable -> {
                                    Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    if (activity != null && !cd.isDisposed()) {
                                        if (throwable instanceof HttpException && ((HttpException) throwable).code() == 401) {
                                            // ログインアクティビティへ遷移
                                            Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                }
                        ));
            });
            snackbar.show();
        });
        // 申請拒否
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            final Snackbar snackbar = Snackbar.make(view, "申請を拒否しますか？", Snackbar.LENGTH_LONG);
            snackbar.setAction("Yes", (v) -> {
                ApiService service = Util.getService();
                HashMap<String, Integer> body = new HashMap<>();
                body.put("user_id", adapter.getList().get(position).id);
                Completable friendList = service.rejectFriendRequest(LoginUser.getToken(), body);
                cd.add(friendList.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .unsubscribeOn(Schedulers.io())
                        .subscribe(
                                () -> {
                                    if(activity != null) {
                                        fetchList();
                                        final Snackbar sbNo = Snackbar.make(view, "申請を拒否しました。", Snackbar.LENGTH_SHORT);
                                        sbNo.getView().setBackgroundColor(Color.BLACK);
                                        sbNo.setActionTextColor(Color.WHITE);
                                        sbNo.show();
                                    }
                                },  // 成功時
                                throwable -> {
                                    Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                                    if(activity != null && !cd.isDisposed()) {
                                        mSwipeRefreshLayout.setRefreshing(false);
                                        if (throwable instanceof HttpException && ((HttpException) throwable).code() == 401) {
                                            // ログインアクティビティへ遷移
                                            Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                }
                        ));

            });
            snackbar.show();
            return true;
        });

        SearchView searchView = activity.findViewById(R.id.searchView_pending);
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
    public void onResume() {
        super.onResume();
        mSwipeRefreshLayout.setRefreshing(false);
        fetchList();
    }

    // Resumeの代わり
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void fetchList() {
        ApiService service = Util.getService();
        Observable<FriendList> friendList;
        friendList = service.getPendedFriendList(LoginUser.getToken());
        cd.add(friendList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if(activity != null){
                                mSwipeRefreshLayout.setRefreshing(false);
                                updateList(list.data);
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            mSwipeRefreshLayout.setRefreshing(false);
                            if(activity != null && !cd.isDisposed()){
                                if (throwable instanceof HttpException && ((HttpException) throwable).code() == 401) {
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
        ArrayList<Friend> list = new ArrayList<>(data);
        ar = new ArrayList<>(data);
        adapter = new MemberAdapter(list);
        // ListViewにadapterをセット
        listView.setAdapter(adapter);
    }

}
