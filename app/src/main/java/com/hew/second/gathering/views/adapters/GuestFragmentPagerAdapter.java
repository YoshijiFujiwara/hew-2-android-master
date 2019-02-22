package com.hew.second.gathering.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hew.second.gathering.fragments.GuestJoinedSessionFragment;
import com.hew.second.gathering.fragments.GuestSessionFragment;

public class GuestFragmentPagerAdapter extends FragmentPagerAdapter {
    protected CharSequence[] tabTitles = {"応答待ち", "参加中"};

    public GuestFragmentPagerAdapter(FragmentManager fm) {
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
                return GuestSessionFragment.newInstance();
            case 1:
                return GuestJoinedSessionFragment.newInstance();
            default:
                return GuestSessionFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }
}
