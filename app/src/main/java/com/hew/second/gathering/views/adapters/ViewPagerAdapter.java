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

//    得たポジション(i)によって切り替える
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

    //  最大値をセット
    @Override
    public int getCount() {
        return lstTitle.size();
    }

    //  tablayoutにlsTitleに追加したTitle情報を渡します
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

