package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

<<<<<<< HEAD
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.SelectedSession;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.BudgetActualListAdapter;
import com.hew.second.gathering.views.adapters.BudgetEstimateListAdapter;
import com.hew.second.gathering.views.adapters.BudgetFragmantPagerAdapter;
=======
import com.hew.second.gathering.views.adapters.BudgetFragmentPagerAdapter;
>>>>>>> develop
import com.hew.second.gathering.R;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BudgetFragment extends Fragment {

    int estimateBudget, actualBudget; // 予算額と実際にかかった金額
    Session session; // session情報
    String name;

    public static BudgetFragment newInstance() {
        return new BudgetFragment();
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
        Activity activity = getActivity();
    }

    private void setSessionInfo(Session data) {
        this.session = data;
        this.name = data.name;
    }
}
