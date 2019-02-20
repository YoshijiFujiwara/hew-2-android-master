package com.hew.second.gathering.fragments;



import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.SelectedSession;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.SessionList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.EventAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;


public class EventFragment extends BaseFragment {
    private static final String MESSAGE = "message";
    ArrayList<EventAdapter.Data> ar = new ArrayList<EventAdapter.Data>();
    private EventAdapter adapter = null;
    private CompositeDisposable cd = new CompositeDisposable();

    public static EventFragment newInstance(String message) {
        EventFragment fragment = new EventFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    public static EventFragment newInstance() {
        EventFragment fragment = new EventFragment();
        return fragment;
    }

    @Override
    public void onDestroy(){
        cd.clear();
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_event, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = activity.findViewById(R.id.fab_newSession);
        fab.setOnClickListener((v) -> createSession());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        activity.setTitle("イベント一覧");
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchList();
    }

    private void fetchList() {
        ApiService service = Util.getService();
        Observable<SessionList> token = service.getSessionList(LoginUser.getToken());
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if(activity != null && !cd.isDisposed()) {
                                updateList(list.data);
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if(activity != null && !cd.isDisposed()) {
                                if (throwable instanceof HttpException && ((HttpException) throwable).code() == 401) {
                                    // ログインアクティビティへ遷移
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                ));
    }

    private void updateList(List<Session> data) {
        // ListView生成
        ListView listView = activity.findViewById(R.id.listView_event);
        ar.clear();
        for (Session m : data) {
            ar.add(new EventAdapter.Data(m.id, m.name, m.shop_id, m.start_time,m.users.size() + "名"));
        }
        adapter = new EventAdapter(ar);
        // ListViewにadapterをセット
        listView.setAdapter(adapter);
    }

    private void createSession(){

        if(activity != null){
            SelectedSession.setSessionId(activity.getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE),-1);
            FragmentManager fragmentManager = ((AppCompatActivity)activity).getSupportFragmentManager();
            if(fragmentManager != null){
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container, EditShopFragment.newInstance());
                fragmentTransaction.commit();
            }
        }
    }
}
