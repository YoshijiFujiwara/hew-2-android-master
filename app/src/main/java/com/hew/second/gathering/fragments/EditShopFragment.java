package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.SearchArgs;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.views.adapters.EditShopFragmentPagerAdapter;
import com.hew.second.gathering.views.adapters.MemberAdapter;
import com.hew.second.gathering.views.adapters.MemberFragmentPagerAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

public class EditShopFragment extends SessionBaseFragment {
    private static final String MESSAGE = "message";
    public static EditShopFragment newInstance() {
        EditShopFragment fragment = new EditShopFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_edit_shop, container, false);
        EditShopFragmentPagerAdapter adapter = new EditShopFragmentPagerAdapter(getChildFragmentManager());
        ViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        activity.setTitle("店舗選択");

        SearchArgs.clear();

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.fragment = "SHOP";

        if(activity.session == null){
            BottomNavigationView bnv = activity.findViewById(R.id.eip_bottom_navigation);
            bnv.setVisibility(View.GONE);
        }
    }

    public void removeFocus() {
        SearchView searchView = activity.findViewById(R.id.searchView_keyword);
        searchView.clearFocus();
    }

}
