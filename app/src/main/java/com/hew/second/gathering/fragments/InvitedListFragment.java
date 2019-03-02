package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.EventProcessMainActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.FriendList;
import com.hew.second.gathering.api.SessionUser;
import com.hew.second.gathering.api.SessionUserList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.MemberAdapter;
import com.hew.second.gathering.views.adapters.SessionMemberAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class InvitedListFragment extends SessionBaseFragment {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView listView = null;
    private SessionMemberAdapter adapter = null;
    private ArrayList<SessionUser> ar = new ArrayList<>();

    public static InvitedListFragment newInstance() {
        return new InvitedListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_invited_list, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout_invited);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        Button inviteEnd = activity.findViewById(R.id.button_invite_end);
        inviteEnd.setOnClickListener((l) -> {
            new MaterialDialog.Builder(activity)
                    .title("セッション")
                    .content("セッションへの招待を打ち切りますか？")
                    .positiveText("OK")
                    .onPositive((dialog, which) -> {
                        abortInvite();
                    })
                    .negativeText("キャンセル")
                    .show();
        });
        fetchList();
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            // 表示状態になったときの処理
            if(activity != null && activity.requestUpdateInvited){
                activity.requestUpdateInvited = false;
                fetchList();
            }
        }
    }

    private void fetchList() {
        mSwipeRefreshLayout.setRefreshing(true);
        ApiService service = Util.getService();
        int id = ((EventProcessMainActivity) activity).session.id;
        if (id == -1) {
            return;
        }
        Observable<SessionUserList> token = service.getSessionUserList(LoginUser.getToken(), id);
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
                            mSwipeRefreshLayout.setRefreshing(false);
                            if (activity != null && !cd.isDisposed() && throwable instanceof HttpException && ((HttpException) throwable).code() == 401) {
                                Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                ));
    }

    private void updateList(List<SessionUser> data) {
        // ListView生成
        listView = activity.findViewById(R.id.member_list_invited);
        if (listView != null) {
            ArrayList<SessionUser> list = new ArrayList<>(data);
            ar = new ArrayList<>(data);
            adapter = new SessionMemberAdapter(list);
            // ListViewにadapterをセット
            listView.setAdapter(adapter);
        }
    }

    private void abortInvite() {
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        activity.requestUpdateInviteGroup = true;
        activity.requestUpdateInviteOne = true;
        ApiService service = Util.getService();
        ArrayList<Completable> addList = new ArrayList<>();
        for (SessionUser s : ar) {
            if (!s.join_status.equals("allow")) {
                addList.add(service.deleteSessionUser(LoginUser.getToken(), activity.session.id, s.id));
            }
        }
        cd.add(Completable
                .concat(addList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            if (activity != null) {
                                dialog.dismiss();
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
