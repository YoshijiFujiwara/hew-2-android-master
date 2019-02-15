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

public class FriendFragment extends Fragment {
    private static final String MESSAGE = "message";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MemberAdapter adapter = null;
    private ArrayList<Friend> ar = new ArrayList<>();
    private ListView listView = null;

    public static FriendFragment newInstance(int kind) {
        FriendFragment fragment = new FriendFragment();

        Bundle args = new Bundle();
        args.putInt(MESSAGE, kind);
        fragment.setArguments(args);

        return fragment;
    }

    public static FriendFragment newInstance() {
        return new FriendFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        return view;
    }


    public void removeFocus() {
        SearchView searchView = getActivity().findViewById(R.id.searchView);
        searchView.clearFocus();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();

        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener((v) -> {
            Intent intent = new Intent(activity.getApplication(), AddMemberActivity.class);
            startActivity(intent);
        });
        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        listView = activity.findViewById(R.id.member_list);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // リストから要素を削除
            Friend deleteFriend = adapter.getList().get(position);
            ar.remove(deleteFriend);
            updateList(ar);
            final Snackbar snackbar = Snackbar.make(getView(), "友達から削除しました。", Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundColor(Color.BLACK);
            snackbar.setActionTextColor(Color.WHITE);
            snackbar.setAction("元に戻す", (v) -> {
                snackbar.dismiss();
            });
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    if (event != DISMISS_EVENT_MANUAL) {
                        ApiService service = Util.getService();
                        Completable friendList = service.deleteFriend(LoginUser.getToken(), deleteFriend.id);
                        friendList.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .unsubscribeOn(Schedulers.io())
                                .subscribe(
                                        () -> {
                                        },  // 成功時
                                        throwable -> {
                                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                                            Util.setLoading(false, getActivity());
                                            // ログインアクティビティへ遷移
                                            Intent intent = new Intent(getActivity().getApplication(), LoginActivity.class);
                                            startActivity(intent);
                                        }
                                );
                    }
                }
            });
            snackbar.show();
        });

        SearchView searchView = activity.findViewById(R.id.searchView);
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
        Util.setLoading(true, getActivity());
        fetchList();
    }

    // Resumeの代わり
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }


    private void fetchList() {
        ApiService service = Util.getService();
        Observable<FriendList> friendList = service.getFriendList(LoginUser.getToken());
        friendList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Util.setLoading(false, getActivity());
                            mSwipeRefreshLayout.setRefreshing(false);
                            updateList(list.data);
                        },  // 成功時
                        throwable -> {
                            Util.setLoading(false, getActivity());
                            mSwipeRefreshLayout.setRefreshing(false);
                            // TODO:エラー処理はこんな形で全てに実装
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (throwable instanceof NullPointerException) {
                                final Snackbar snackbar = Snackbar.make(getView(), "データがありません。", Snackbar.LENGTH_LONG);
                                snackbar.getView().setBackgroundColor(Color.BLACK);
                                snackbar.setActionTextColor(Color.WHITE);
                                snackbar.show();
                            } else if (throwable instanceof HttpException && ((HttpException) throwable).code() == 401) {
                                // ログインアクティビティへ遷移
                                Intent intent = new Intent(getActivity().getApplication(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                );
    }

    private void updateList(List<Friend> data) {
        // ListView生成
        listView = getActivity().findViewById(R.id.member_list);
        ArrayList<Friend> list = new ArrayList<>(data);
        ar = new ArrayList<>(data);
        adapter = new MemberAdapter(list);
        // ListViewにadapterをセット
        listView.setAdapter(adapter);
    }

}
