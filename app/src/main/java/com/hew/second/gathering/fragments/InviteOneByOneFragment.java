package com.hew.second.gathering.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.AddMemberActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.FriendList;
import com.hew.second.gathering.api.GroupDetail;
import com.hew.second.gathering.api.SessionUser;
import com.hew.second.gathering.api.SessionUserList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.GroupMemberAdapter;
import com.hew.second.gathering.views.adapters.MemberAdapter;
import com.hew.second.gathering.views.adapters.SessionAddMemberAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static android.app.Activity.RESULT_OK;
import static com.hew.second.gathering.activities.BaseActivity.SNACK_MESSAGE;

public class InviteOneByOneFragment extends SessionBaseFragment {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SessionAddMemberAdapter adapter = null;
    private ArrayList<Friend> ar = new ArrayList<>();
    private GridView gridView = null;


    public static InviteOneByOneFragment newInstance() {
        return new InviteOneByOneFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_invite_one_by_one, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout_invite_one);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        gridView = activity.findViewById(R.id.gridView_one);
        gridView.setChoiceMode(gridView.CHOICE_MODE_MULTIPLE);
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if (adapter.getChecked(position)) {
                adapter.setChecked(position,false);
            } else {
                adapter.setChecked(position,true);
            }
            adapter.notifyDataSetChanged();
        });

        Button inviteOne = activity.findViewById(R.id.button_invite_one);
        inviteOne.setOnClickListener((l) -> {
            if(adapter.getCheckedCount() <= 0){
                final Snackbar snackbar = Snackbar.make(view, "招待する人を選択してください。", Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(Color.BLACK);
                snackbar.setActionTextColor(Color.WHITE);
                snackbar.show();
                return;
            }
            new MaterialDialog.Builder(activity)
                    .title("イベントへ追加")
                    .content(adapter.getCheckedCount() + "名をイベントに招待しますか？")
                    .positiveText("OK")
                    .onPositive((dialog, which) -> {
                        createSessionUser();
                    })
                    .negativeText("キャンセル")
                    .show();
        });

        SearchView searchView = activity.findViewById(R.id.searchView_invite_one);
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
                adapter.clear();
                adapter.addAll(filteredItems);
                adapter.notifyDataSetChanged();
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
        fetchList();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            // 表示状態になったときの処理
            if(activity != null && activity.requestUpdateInviteOne){
                activity.requestUpdateInviteOne = false;
                fetchList();
            }
        }
    }

    private void fetchList() {
        mSwipeRefreshLayout.setRefreshing(true);
        ApiService service = Util.getService();
        Observable<FriendList> friendList = service.getAddableToSessionFriendList(LoginUser.getToken(), activity.session.id);
        cd.add(friendList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            if (activity != null && !cd.isDisposed()) {
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
        gridView = activity.findViewById(R.id.gridView_one);
        if (gridView != null) {
            ArrayList<Friend> list = new ArrayList<>(data);
            ar = new ArrayList<>(data);
            adapter = new SessionAddMemberAdapter(list);
            // ListViewにadapterをセット
            gridView.setAdapter(adapter);
        }
    }

    private void createSessionUser(){
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        activity.requestUpdateInviteGroup = true;
        activity.requestUpdateInvited = true;
        List<Boolean> sba = adapter.getCheckedList();
        List<Friend> work = adapter.getList();
        ApiService service = Util.getService();
        ArrayList<Observable<SessionUserList>> addList = new ArrayList<>();
        for (int i = 0; i < sba.size(); i++) {
            if(sba.get(i)){
                HashMap<String, String> body = new HashMap<>();
                body.put("user_id", String.valueOf(work.get(i).id));
                addList.add(service.createSessionUser(LoginUser.getToken(), activity.session.id, body));
            }
        }
        cd.add(Observable
                .concat(addList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if(activity != null){
                                activity.session.users = new ArrayList<>(list.data);
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                                dialog.dismiss();
                                fetchList();
                            }
                        },
                        ()->{
                            if (activity != null) {
                                dialog.dismiss();
                                final Snackbar snackbar = Snackbar.make(view, "招待を送信しました。", Snackbar.LENGTH_SHORT);
                                snackbar.getView().setBackgroundColor(Color.BLACK);
                                snackbar.setActionTextColor(Color.WHITE);
                                snackbar.show();
                                fetchList();
                            }
                        }
                ));
    }
}
