package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.views.adapters.EditShopFragmentPagerAdapter;
import com.hew.second.gathering.views.adapters.MemberAdapter;
import com.hew.second.gathering.views.adapters.MemberFragmentPagerAdapter;

import java.util.ArrayList;

public class EditShopFragment extends BaseFragment {
    private static final String MESSAGE = "message";

    public static EditShopFragment newInstance(String message) {
        EditShopFragment fragment = new EditShopFragment();

        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

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

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final String[] texts = {
                // Globe Decade の楽曲リストより
                "Feel Like dance",
                "Joy to the love (globe)",
                "SWEET PAIN",
                "DEPARTURES (RADIO EDIT)",
                "FREEDOM (RADIO EDIT)",
                "Is this love",
                "Can't Stop Fallin' in Love",
                "FACE",
                "FACES PLACES",
                "Anytime smokin' cigarette",
                "Wanderin' Destiny",
                "Love again",
                "wanna Be A Dreammaker",
                "Sa Yo Na Ra",
                "sweet heart",
                "Perfume of love",
                "MISS YOUR BODY",
                "still growin' up",
                "biting her nails",
                "とにかく無性に…"
        };

        // itemを表示するTextViewが設定されているlist.xmlを指す
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(activity, R.layout.text_cell);

        // activity_main.xmlのlistViewにListViewをセット
        ListView listView = activity.findViewById(R.id.listView_shop_list);
        for (String str: texts){
            // ArrayAdapterにitemを追加する
            arrayAdapter.add(str);
        }

        // adapterをListViewにセット
        listView.setAdapter(arrayAdapter);
    }

    public void removeFocus() {
        /*
        SearchView searchView = getActivity().findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView = getActivity().findViewById(R.id.searchView_applying);
        searchView.clearFocus();
        searchView = getActivity().findViewById(R.id.searchView_pending);
        searchView.clearFocus();
        */
    }

}
