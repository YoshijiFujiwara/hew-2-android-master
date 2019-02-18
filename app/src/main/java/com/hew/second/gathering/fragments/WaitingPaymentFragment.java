package com.hew.second.gathering.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
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
//支払い待ち
public class WaitingPaymentFragment extends Fragment {

    ListView listview ;
    int i = 0;

    public WaitingPaymentFragment() {
    }

    public static WaitingPaymentFragment newInstance() {
        return new WaitingPaymentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_waiting_payment,container,false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



    }

    @Override
    public void onResume() {
        super.onResume();
        fetchSessionList();
    }

    //    セッション情報取得
    public void fetchSessionList() {

        ApiService service = Util.getService();
        Observable<SessionList> sessionList;
        sessionList = service.getSessionList(LoginUser.getToken());
        sessionList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
//                          表示
                            updateList(list.data);

                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());


                        }
                );
    }

    public void updateList(List<Session> data) {


        ListView listView = getActivity().findViewById(R.id.listView_waitingPay);
        ImageView imageView = getActivity().findViewById(R.id.session_image);
        ArrayList<Session> sessionArrayList = new ArrayList<>();

        for (Session sl : data) {
            for (int j = 0;j <sl.users.size();j++ ) {
                if (sl.users.get(j).paid == 1) {
                    imageView.setImageResource(R.drawable.ic_warning);
                }
            }
            sessionArrayList.add(sl);
        }
        SessionAdapter adapter = new SessionAdapter(sessionArrayList);
        listView.setAdapter(adapter);

    }


}
