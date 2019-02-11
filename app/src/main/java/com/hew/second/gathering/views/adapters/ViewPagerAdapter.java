package com.hew.second.gathering.views.adapters;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hew.second.gathering.fragments.HistoryFragment;
import com.hew.second.gathering.fragments.InProgressFragment;
import com.hew.second.gathering.fragments.WaitingPaymentFragment;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<String> lstTitle = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return InProgressFragment.newInstance();
            case 1:
                return WaitingPaymentFragment.newInstance();
            case 2:
                return HistoryFragment.newInstance();
            default:
                return InProgressFragment.newInstance();
        }
    }

    //  要素数取得
    @Override
    public int getCount() {
        return lstTitle.size();
    }

    //  要素番号取得
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return lstTitle.get(position);
    }

    //  Fragment要素を追加
    public void AddFragment(String title) {
        lstTitle.add(title);
    }
}

