package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.EventProcessMainActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.SessionAdapter;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

//セッション一覧進行中のセッション
public class InProgressFragment extends BaseFragment {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    SessionAdapter adapter;
    ArrayList<Session> sessionArrayList;
    ListView listView;

    public static InProgressFragment newInstance() {
        return new InProgressFragment();
    }

    public InProgressFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_in_progress, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceStat) {
        super.onActivityCreated(savedInstanceStat);

        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener((v) -> createSession());

        listView = getActivity().findViewById(R.id.listView_in_progress);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(activity.getApplication(), EventProcessMainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("SESSION_DETAIL", Parcels.wrap(sessionArrayList.get(position)));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout_progress);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> updateSessionList());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSessionList();

    }

    public void updateSessionList() {
        mSwipeRefreshLayout.setRefreshing(true);
        ApiService service = Util.getService();
        Observable<SessionList> sessionList;
        sessionList = service.getSessionList(LoginUser.getToken());
        cd.add(sessionList.subscribeOn(Schedulers.io())
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
                            if (activity != null && !cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    // ログインアクティビティへ遷移
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                ));
    }

    public void updateList(List<Session> data) {
        sessionArrayList = new ArrayList<>();
        //開始時刻がセットされて終了時刻がNULL
        for (Session sl : data) {
            if (sl.start_time != null && sl.end_time == null) {
                sessionArrayList.add(sl);
            }
        }
        SessionAdapter adapter = new SessionAdapter(sessionArrayList);
        if (listView != null) {
            listView.setAdapter(adapter);
        }

    }

    private void createSession() {
        //遷移
        Intent intent = new Intent(activity.getApplication(), EventProcessMainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("FRAGMENT", "SHOP");
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
