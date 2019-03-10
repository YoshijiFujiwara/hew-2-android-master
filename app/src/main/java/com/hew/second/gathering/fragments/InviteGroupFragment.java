package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.GridView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.Group;
import com.hew.second.gathering.api.GroupList;
import com.hew.second.gathering.api.SessionUser;
import com.hew.second.gathering.api.SessionUserList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.InviteGroupAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class InviteGroupFragment extends SessionBaseFragment {


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private InviteGroupAdapter adapter = null;
    private ArrayList<Group> ar = new ArrayList<>();
    private GridView gridView = null;

    public static InviteGroupFragment newInstance() {
        return new InviteGroupFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_invite_group, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout_invite_group);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        gridView = activity.findViewById(R.id.gridView_group);
        gridView.setChoiceMode(gridView.CHOICE_MODE_SINGLE);
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            if(adapter.getChecked(position)){
                adapter.setChecked(position,false);
            } else {
                adapter.clearChecked();
                adapter.setChecked(position,true);
            }
            adapter.notifyDataSetChanged();
        });

        Button inviteGroup = activity.findViewById(R.id.button_invite_group);
        inviteGroup.setOnClickListener((l) -> {
            if(adapter.getCheckedPos() == null){
                final Snackbar snackbar = Snackbar.make(view, "招待するグループを選択してください。", Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(Color.BLACK);
                snackbar.setActionTextColor(Color.WHITE);
                snackbar.show();
                return;
            }
            new MaterialDialog.Builder(activity)
                    .title(activity.session.name)
                    .content(ar.get(adapter.getCheckedPos()).name + "をイベントに招待しますか？")
                    .positiveText("OK")
                    .onPositive((dialog, which) -> {
                        createSessionGroup();
                    })
                    .negativeText("キャンセル")
                    .show();
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
            if(activity != null && activity.requestUpdateInviteGroup){
                activity.requestUpdateInviteGroup = false;
                fetchList();
            }
        }
    }

    private void fetchList() {
        ApiService service = Util.getService();
        Observable<GroupList> token = service.getAddableToSessionGroupList(activity.session.id);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (activity != null) {
                                mSwipeRefreshLayout.setRefreshing(false);
                                updateList(list.data);
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed() && throwable instanceof HttpException && ((HttpException) throwable).code() == 401) {
                                Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                ));
    }

    private void updateList(List<Group> data) {
        // ListView生成
        GridView gridView = activity.findViewById(R.id.gridView_group);
        ar.clear();
        ar.addAll(data);
        adapter = new InviteGroupAdapter(ar);
        if(gridView != null){
            // ListViewにadapterをセット
            gridView.setAdapter(adapter);
        }
    }

    private void createSessionGroup(){
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        activity.requestUpdateInviteOne = true;
        activity.requestUpdateInvited = true;
        int pos = gridView.getCheckedItemPosition();
        ApiService service = Util.getService();
        Observable<SessionUserList> user = service.createSessionGroup(activity.session.id, ar.get(pos).id);
        cd.add(user
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (activity != null) {
                                activity.session.users = new ArrayList<>(list.data);
                                dialog.dismiss();
                                final Snackbar snackbar = Snackbar.make(view, "招待を送信しました。", Snackbar.LENGTH_SHORT);
                                snackbar.getView().setBackgroundColor(Color.BLACK);
                                snackbar.setActionTextColor(Color.WHITE);
                                snackbar.show();
                                fetchList();
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
                        }
                ));

    }
}
