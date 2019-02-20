package com.hew.second.gathering.fragments;



import android.support.design.widget.FloatingActionButton;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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


public class EventFragment extends Fragment {
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

        View view = inflater.inflate(R.layout.fragment_event, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = getActivity().findViewById(R.id.fab_newSession);
        fab.setOnClickListener((v) -> createSession());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Activity activity = getActivity();
        activity.setTitle("イベント一覧");
    }

    @Override
    public void onResume() {
        super.onResume();
        Util.setLoading(true, getActivity());
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
                            Util.setLoading(false, getActivity());
                            updateList(list.data);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            Util.setLoading(false, getActivity());
                            // ログインアクティビティへ遷移
                            Intent intent = new Intent(getActivity().getApplication(), LoginActivity.class);
                            startActivity(intent);
                        }
                ));
    }

    private void updateList(List<Session> data) {
        // ListView生成
        ListView listView = getActivity().findViewById(R.id.listView_event);
        ar.clear();
        for (Session m : data) {
            ar.add(new EventAdapter.Data(m.id, m.name, m.shop_id, m.start_time,m.users.size() + "名"));
        }
        adapter = new EventAdapter(ar);
        // ListViewにadapterをセット
        listView.setAdapter(adapter);
    }

    private void createSession(){
        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("name", "セッション");
        Observable<SessionDetail> token = service.createSession(LoginUser.getToken(),body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if(getActivity() != null){
                                Util.setLoading(false, getActivity());
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                if(fragmentManager != null){
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.addToBackStack(null);
                                    fragmentTransaction.replace(R.id.container, EditShopFragment.newInstance());
                                    fragmentTransaction.commit();
                                }
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if(getActivity() != null) {
                                Util.setLoading(false, getActivity());
                                // ログインアクティビティへ遷移
                                Intent intent = new Intent(getActivity().getApplication(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                ));

    }
}
