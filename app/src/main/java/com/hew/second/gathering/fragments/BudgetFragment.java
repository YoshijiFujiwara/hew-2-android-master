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
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.BudgetFragmentPagerAdapter;
import com.hew.second.gathering.R;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class BudgetFragment extends SessionBaseFragment {
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentActivity fragmentActivity = activity;
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
        FragmentActivity fragmentActivity = activity;
        if (fragmentActivity != null) {
            Util.setLoading(true, fragmentActivity);
        }
        Log.v("message", "BudgetFragmentOnResume");

    }
}
