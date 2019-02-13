package com.hew.second.gathering.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hew.second.gathering.R;
import com.hew.second.gathering.views.adapters.InviteFragmentPagerAdapter;

public class InviteFragment extends Fragment {

    public static InviteFragment newInstance() {
        return new InviteFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        InviteFragmentPagerAdapter adapter = new InviteFragmentPagerAdapter(getChildFragmentManager());
        ViewPager viewPager = getView().findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout = getView().findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }
}
