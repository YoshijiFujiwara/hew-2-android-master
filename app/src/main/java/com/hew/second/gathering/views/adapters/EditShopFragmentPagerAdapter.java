package com.hew.second.gathering.views.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hew.second.gathering.fragments.ApplyingFragment;
import com.hew.second.gathering.fragments.FriendFragment;
import com.hew.second.gathering.fragments.MapFragment;
import com.hew.second.gathering.fragments.PendingFragment;
import com.hew.second.gathering.fragments.SearchShopFragment;

public class EditShopFragmentPagerAdapter extends FragmentPagerAdapter {
    protected CharSequence[] tabTitles = {"検索条件", "マップ"};

    public EditShopFragmentPagerAdapter(FragmentManager fm) {
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
                return SearchShopFragment.newInstance();
            case 1:
                return MapFragment.newInstance();
            default:
                return MapFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }
}
