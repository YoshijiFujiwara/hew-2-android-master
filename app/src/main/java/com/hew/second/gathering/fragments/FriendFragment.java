package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.AddMemberActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.activities.MainActivity;
import com.hew.second.gathering.activities.MemberDetailActivity;
import com.hew.second.gathering.activities.ShopDetailActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.FriendList;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.MemberAdapter;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static com.hew.second.gathering.activities.BaseActivity.INTENT_FRIEND_DETAIL;
import static com.hew.second.gathering.activities.BaseActivity.INTENT_SHOP_DETAIL;

public class FriendFragment extends BaseFragment {
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

        view = inflater.inflate(R.layout.fragment_friend, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
            if (view.getId() == R.id.member_delete) {
                new MaterialDialog.Builder(activity)
                        .title("友達解除")
                        .content(ar.get(position).username + "さんとの友達登録を解除しますか？")
                        .positiveText("OK")
                        .onPositive((dialog, which) -> {
                            deleteFriend(ar.get(position).id);
                        })
                        .negativeText("キャンセル")
                        .show();
            } else {
                Intent intent = new Intent(activity.getApplication(), MemberDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("FRIEND_DETAIL", Parcels.wrap(ar.get(position)));
                intent.putExtras(bundle);
                startActivityForResult(intent, INTENT_FRIEND_DETAIL);
            }
        });

        SearchView searchView = activity.findViewById(R.id.searchView);
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
        Observable<FriendList> friendList = service.getFriendList(LoginUser.getToken());
        cd.add(friendList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            if (activity != null && !cd.isDisposed()) {
                                ar = new ArrayList<>(list.data);
                                updateList(list.data);
                            }
                        },  // 成功時
                        throwable -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                if (throwable instanceof NullPointerException) {
                                    final Snackbar snackbar = Snackbar.make(view, "データがありません。", Snackbar.LENGTH_LONG);
                                    snackbar.getView().setBackgroundColor(Color.BLACK);
                                    snackbar.setActionTextColor(Color.WHITE);
                                    snackbar.show();
                                } else if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
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
        listView = activity.findViewById(R.id.member_list);
        if (listView != null) {
            ArrayList<Friend> list = new ArrayList<>(data);
            adapter = new MemberAdapter(list);
            if (listView != null) {
                // ListViewにadapterをセット
                listView.setAdapter(adapter);
            }
        }
    }

    public void deleteFriend(int id) {
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        ApiService service = Util.getService();
        Completable friendList = service.deleteFriend(LoginUser.getToken(), id);
        cd.add(friendList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            if (activity != null) {
                                dialog.dismiss();
                                final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "友達登録を解除しました。", Snackbar.LENGTH_SHORT);
                                snackbar.getView().setBackgroundColor(Color.BLACK);
                                snackbar.setActionTextColor(Color.WHITE);
                                snackbar.show();
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


}
