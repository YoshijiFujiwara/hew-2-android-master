package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.SelectedSession;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.BudgetActualListAdapter;
import com.hew.second.gathering.views.adapters.BudgetEstimateListAdapter;
import com.hew.second.gathering.views.adapters.BudgetFragmentPagerAdapter;
import com.hew.second.gathering.R;
import com.hew.second.gathering.views.adapters.EventAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BudgetFragment extends Fragment {
    private static final String BUDGET_MESSAGE = "budget_message";
    private static final String SESSION_DETAIL = "session_detail";

    int estimateBudget, actualBudget; // 予算額と実際にかかった金額
    Session session; // session情報
    String name;

    public static BudgetFragment newInstance(String message) {
        BudgetFragment fragment = new BudgetFragment();

        Bundle args = new Bundle();
        args.putString(BUDGET_MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    public static BudgetFragment newInstance() {
        BudgetFragment fragment = new BudgetFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // todo sessionidを1にセット
        SelectedSession.setSessionId(getActivity().getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE), 1);
        Log.v("sessionid", "" + SelectedSession.getSharedSessionId(getActivity().getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)));

        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
        BudgetFragmentPagerAdapter adapter = new BudgetFragmentPagerAdapter(getChildFragmentManager());
        ViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 多分ロータがくるくる回る
        Util.setLoading(true, getActivity());
        fetchList();
    }

    // sharedPreferenceに入っているセッションのIDから、
    // 予算画面に必要な情報を取得する
    private void fetchList() {
        ApiService service = Util.getService();
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    // sharedPreferenceからセッションIDを取得する
                    return service.getSessionDetail(LoginUser.getToken(), SelectedSession.getSharedSessionId(getActivity().getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Util.setLoading(false, getActivity());
                            distributeSessionInfo(list.data);
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

    // セッション情報を子フラグメントたちへ渡す
    private void distributeSessionInfo(Session data) {
        // ブサイクやけど２回に分ける
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        // 予算の中の予算の方のフラグメント
        BudgetEstimateFragment budgetEstimateFragment = new BudgetEstimateFragment();

        Bundle bundle = new Bundle();
        // 渡すオブジェクトをセット
        bundle.putSerializable(SESSION_DETAIL, (Serializable) data);
        budgetEstimateFragment.setArguments(bundle);
        ft.replace(android.R.id.content, budgetEstimateFragment);
        ft.addToBackStack(null);
        ft.commit();



        FragmentTransaction ft2 = getActivity().getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        // 予算の中の予算の方のフラグメント
        BudgetActualFragment budgetActualFragment = new BudgetActualFragment();

        Bundle bundle2 = new Bundle();
        // 渡すオブジェクトをセット
        bundle2.putSerializable(SESSION_DETAIL, (Serializable) data);
        budgetEstimateFragment.setArguments(bundle);
        ft.replace(android.R.id.content, budgetEstimateFragment);
        ft.addToBackStack(null);
        ft.commit();
    }
}
