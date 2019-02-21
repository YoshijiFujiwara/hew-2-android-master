package com.hew.second.gathering.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.SelectedSession;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.BudgetFragmentPagerAdapter;
import com.hew.second.gathering.R;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BudgetFragment extends Fragment {
    protected static final String BUDGET_MESSAGE = "budget_message";
    protected static final String SESSION_DETAIL = "session_detail";

    int estimateBudget, actualBudget; // 予算額と実際にかかった金額
    Session session; // session情報
    String name;

    public static BudgetFragment newInstance(String message) {
        BudgetFragment fragment = new BudgetFragment();
        return fragment;
    }

    public static BudgetFragment newInstance() {
        BudgetFragment fragment = new BudgetFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null) {
            // todo とりあえず、セッションIDを１に設定
            SelectedSession.setSessionId(getActivity().getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE), 1);
            Toast.makeText(getActivity(), String.valueOf(SelectedSession.getSharedSessionId(getActivity().getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE))), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getSessionDetailFromSP();
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity != null) {
            View view = inflater.inflate(R.layout.fragment_budget, container, false);
            BudgetFragmentPagerAdapter adapter = new BudgetFragmentPagerAdapter(getChildFragmentManager());
            ViewPager viewPager = view.findViewById(R.id.viewPager);
            viewPager.setOffscreenPageLimit(2);
            viewPager.setAdapter(adapter);

            TabLayout tabLayout = view.findViewById(R.id.tabLayout);
            tabLayout.setupWithViewPager(viewPager);

            return view;
        }
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 多分ロータがくるくる回る
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity != null) {
            Util.setLoading(true, fragmentActivity);
        }
        Log.v("message", "BudgetFragmentOnResume");

    }

    // sharedPreferenceに入っているセッションのIDから、
    // 予算画面に必要な情報を取得する
    public void getSessionDetailFromSP() {
        // todo sessionidを1にセット
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity != null) {
            Toast.makeText(fragmentActivity, "テスト", Toast.LENGTH_LONG).show();
            ApiService service = Util.getService();
            Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
            token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    // sharedPreferenceからセッションIDを取得する
                    return service.getSessionDetail(LoginUser.getToken(), SelectedSession.getSharedSessionId(fragmentActivity.getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                    list -> {
                        Util.setLoading(false, fragmentActivity);
                        Log.v("sessioninfo", list.data.name);

                        // sharedPreferenceにsessionの詳細情報を渡す
                        SelectedSession.setSessionDetail(fragmentActivity.getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE), list.data);

                    },  // 成功時
                    throwable -> {
                        Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                        Util.setLoading(false, fragmentActivity);
                        // ログインアクティビティへ遷移
                        Intent intent = new Intent(fragmentActivity.getApplication(), LoginActivity.class);
                        startActivity(intent);
                    }
                );
        }

    }
}
