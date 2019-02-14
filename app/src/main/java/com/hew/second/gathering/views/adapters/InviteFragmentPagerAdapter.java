package com.hew.second.gathering.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hew.second.gathering.fragments.BudgetActualFragment;
import com.hew.second.gathering.fragments.BudgetEstimateFragment;
import com.hew.second.gathering.fragments.InviteGroupFragment;
import com.hew.second.gathering.fragments.InviteOneByOneFragment;
import com.hew.second.gathering.fragments.InvitedListFragment;

public class InviteFragmentPagerAdapter extends FragmentPagerAdapter {
    protected CharSequence[] tabTitles = {"招待済み", "1人1人招待","グループに招待"};

    public InviteFragmentPagerAdapter(FragmentManager fm) {
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
                return InvitedListFragment.newInstance();
            case 1:
                return InviteOneByOneFragment.newInstance();
            case 2:
                return InviteGroupFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }
}
