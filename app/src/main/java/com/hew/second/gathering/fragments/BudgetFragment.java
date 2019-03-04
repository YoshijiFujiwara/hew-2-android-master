package com.hew.second.gathering.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.BudgetFragmentPagerAdapter;
import com.hew.second.gathering.R;

import dmax.dialog.SpotsDialog;

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

    public void removeFocus() {
        InputMethodManager inputMethodMgr = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
        inputMethodMgr.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity.setTitle("予算管理");
        FragmentActivity fragmentActivity = activity;
        if (fragmentActivity != null) {
            view = inflater.inflate(R.layout.fragment_budget, container, false);
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
        activity.fragment = "BUDGET";
    }

    @Override
    public void onResume() {
        super.onResume();
        FragmentActivity fragmentActivity = activity;
        Log.v("message", "BudgetFragmentOnResume");

    }
}
