package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.GuestSessionAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class GuestJoinedSessionFragment extends BaseFragment {
    private static final String MESSAGE = "message";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GuestSessionAdapter adapter = null;
    private ArrayList<Session> ar = new ArrayList<>();
    private ListView listView = null;

    public static GuestJoinedSessionFragment newInstance() {
        return new GuestJoinedSessionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_guest_joined, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout_guest_joined);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        mSwipeRefreshLayout.setRefreshing(true);
        fetchList();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // Resumeの代わり
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void fetchList() {
        ApiService service = Util.getService();
        Observable<SessionList> sessionList = service.getGuestSessionList(LoginUser.getToken());
        cd.add(sessionList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if(activity != null){
                                mSwipeRefreshLayout.setRefreshing(false);
                                //updateList(list.data);
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
/*
    private void updateList(List<Session> data) {
        // ListView生成
        listView = activity.findViewById(R.id.listView_guest_joined);
        ArrayList<Session> list = new ArrayList<>(data);
        ar = new ArrayList<>(data);
        adapter = new GuestSessionAdapter(list);
        // ListViewにadapterをセット
        listView.setAdapter(adapter);
    }
    */

}
