package com.hew.second.gathering.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hew.second.gathering.fragments.BudgetActualFragment;
import com.hew.second.gathering.fragments.BudgetEstimateFragment;

public class BudgetFragmantPagerAdapter extends FragmentPagerAdapter {
    protected CharSequence[] tabTitles = {"予算計算", "実費と支払い"};

    public BudgetFragmantPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new BudgetEstimateFragment();
            case 1:
                return new BudgetActualFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }
}
