package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.SessionAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class InProgressFragment extends Fragment {

    public static InProgressFragment newInstance() {
        return new InProgressFragment();
    }

    public InProgressFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_in_progress,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        updateSessionList();
    }

    public void updateSessionList() {

        ApiService service = Util.getService();
        Observable<SessionList> sessionList;
        sessionList = service.getSessionList(LoginUser.getToken());

        sessionList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
//                          表示
                            List<Session> i = list.data;
                            ListView listView = getActivity().findViewById(R.id.listView_in_progress);
                            updateList(list.data,listView);

//                            ListViewのクリックイベント処理
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Bundle bundle = new Bundle();
                                    Fragment fragment = new EventProcessMainFragment();
//                                    押された場所のデータを格納予定
//                                  List<Session> sessions = (List<Session>) list.data.get(position);
//                                  bundle.putParcelable("session", Parcels.wrap(sessions));
//                                  fragment.setArguments(bundle);

//                                  Activity呼び出しの場合
                                  Intent intent = new Intent(getActivity(), EventProcessMainActivity.class);
                                  startActivity(intent);

//                                    Fragment呼び出しの場合
//                                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                                    fragmentTransaction.replace(R.id.drawer_layout,fragment);
//                                    fragmentTransaction.addToBackStack(null);
//                                    fragmentTransaction.commit();

                                }
                            });
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                        }
                );
    }

    public void updateList(List<Session> data ,ListView listView) {



        ArrayList<Session> sessionArrayList = new ArrayList<>();
//      開始時刻がセットされて終了時刻が？？？
        for (Session sl : data) {
            if (sl.start_time != null && sl.end_time == null) {
                sessionArrayList.add(sl);
            }
        }

        SessionAdapter adapter = new SessionAdapter(sessionArrayList);
        listView.setAdapter(adapter);

    }
}
