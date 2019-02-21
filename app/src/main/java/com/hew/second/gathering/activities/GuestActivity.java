package com.hew.second.gathering.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;

import com.hew.second.gathering.R;
import com.hew.second.gathering.views.adapters.GuestFragmentPagerAdapter;
import com.hew.second.gathering.views.adapters.MemberFragmentPagerAdapter;

public class GuestActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);

        // Backボタンを有効にする
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        GuestFragmentPagerAdapter adapter = new GuestFragmentPagerAdapter(getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        setTitle("招待画面");

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }
}
