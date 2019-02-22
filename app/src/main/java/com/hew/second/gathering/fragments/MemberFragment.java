package com.hew.second.gathering.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hew.second.gathering.views.adapters.MemberAdapter;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.views.adapters.MemberFragmentPagerAdapter;

import java.util.ArrayList;

public class MemberFragment extends BaseFragment {
    private static final String MESSAGE = "message";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MemberAdapter adapter = null;
    private ArrayList<Friend> ar = new ArrayList<>();
    private ListView listView = null;

    public static MemberFragment newInstance(String message) {
        MemberFragment fragment = new MemberFragment();

        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    public static MemberFragment newInstance() {
        MemberFragment fragment = new MemberFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_member, container, false);
        MemberFragmentPagerAdapter adapter = new MemberFragmentPagerAdapter(getChildFragmentManager());
        ViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        activity.setTitle("メンバー一覧");

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    public void removeFocus() {
        SearchView searchView = activity.findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView = activity.findViewById(R.id.searchView_applying);
        searchView.clearFocus();
        searchView = activity.findViewById(R.id.searchView_pending);
        searchView.clearFocus();
    }

}
