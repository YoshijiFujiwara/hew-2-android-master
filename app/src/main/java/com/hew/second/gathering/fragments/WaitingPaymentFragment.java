package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import static com.hew.second.gathering.R.layout;

//セッション一覧支払い待ち
public class WaitingPaymentFragment extends BaseFragment {


    int i = 0;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    public WaitingPaymentFragment() {
    }


    public static WaitingPaymentFragment newInstance() {
        return new WaitingPaymentFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(layout.fragment_waiting_payment, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceStat) {
        super.onActivityCreated(savedInstanceStat);

        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout_waiting_payment);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchSessionList());
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchSessionList();
    }

    //    セッション情報取得
    public void fetchSessionList() {
        mSwipeRefreshLayout.setRefreshing(true);
        ApiService service = Util.getService();
        Observable<SessionList> sessionList;
        sessionList = service.getSessionList(LoginUser.getToken());
        cd.add(sessionList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            if (activity != null) {
                                ListView listView = getActivity().findViewById(R.id.listView_waitingPay);
                                updateList(list.data, listView);
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

    public void updateList(List<Session> data, ListView listView) {

        int paidcheck = 0;

        ArrayList<Session> sessionArrayList = new ArrayList<>();

//       支払ってない場合追加
        for (Session sl : data) {
            for (int i = 0; i < sl.users.size(); i++) {
                if (sl.users.get(i).paid == 1) {
                    paidcheck++;
                }
            }

            if (paidcheck != sl.users.size()) {
                sessionArrayList.add(sl);
            }
            paidcheck = 0;
        }

        SessionAdapter adapter = new SessionAdapter(sessionArrayList);

        if (listView != null) {
            listView.setAdapter(adapter);
        }
    }

}
