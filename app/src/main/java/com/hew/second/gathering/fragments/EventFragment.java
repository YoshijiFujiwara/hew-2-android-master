package com.hew.second.gathering.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.AddMemberActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.Group;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.EventAdapter;
import com.hew.second.gathering.views.adapters.GroupAdapter;
import com.hew.second.gathering.views.adapters.MemberAdapter;
import com.hew.second.gathering.views.adapters.SessionAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class EventFragment extends Fragment {
    private static final String MESSAGE = "message";
    ArrayList<EventAdapter.Data> ar = new ArrayList<EventAdapter.Data>();
    private EventAdapter adapter = null;

    public static EventFragment newInstance(String message) {
        EventFragment fragment = new EventFragment();

        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    public static EventFragment newInstance() {
        EventFragment fragment = new EventFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    return service.getSessionList(LoginUser.getToken());
                })
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
                );
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

}
