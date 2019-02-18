package com.hew.second.gathering.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hew.second.gathering.fragments.ApplyingFragment;
import com.hew.second.gathering.fragments.FriendFragment;
import com.hew.second.gathering.fragments.PendingFragment;

public class MemberFragmentPagerAdapter extends FragmentPagerAdapter {
    protected CharSequence[] tabTitles = {"友達", "申請中","承認待ち"};

    public MemberFragmentPagerAdapter(FragmentManager fm) {
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
                return FriendFragment.newInstance();
            case 1:
                return ApplyingFragment.newInstance();
            case 2:
                return PendingFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }
}
