package com.hew.second.gathering.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.hew.second.gathering.R;
import com.hew.second.gathering.fragments.EventFinishFragment;
import com.hew.second.gathering.fragments.MemberSendFragment;
import com.hew.second.gathering.fragments.ReservationPhoneFragment;
import com.hew.second.gathering.fragments.StartTimeFragment;

public class EventProcessMainTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_process_main_test);

        BottomNavigationView bnv = findViewById(R.id.eip_bottom_navigation);

        if (savedInstanceState == null ) {
            // FragmentManagerのインスタンス生成
            FragmentManager fragmentManager = getSupportFragmentManager();
            // FragmentTransactionのインスタンスを取得
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // インスタンスに対して張り付け方を指定する
            fragmentTransaction.replace(R.id.eip_container, EventFinishFragment.newInstance());
            // 張り付けを実行
            fragmentTransaction.commit();
        }
//      ボトムズバー選択時
        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.navi_boto_main) {

                    FragmentManager fragmentManager = getSupportFragmentManager();

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.replace(R.id.eip_container, EventFinishFragment.newInstance());

                    fragmentTransaction.commit();

                } else if (id == R.id.navi_botto_budget) {
                    FragmentManager fragmentManager = getSupportFragmentManager();

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//
//                    fragmentTransaction.replace(R.id.eip_container, Bud.newInstance());

                    fragmentTransaction.commit();

                }else if (id == R.id.navi_botto_reservation) {
                    FragmentManager fragmentManager = getSupportFragmentManager();

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.replace(R.id.eip_container, ReservationPhoneFragment.newInstance());

                    fragmentTransaction.commit();

                }else if (id == R.id.navi_botto_member) {
                    FragmentManager fragmentManager = getSupportFragmentManager();

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.replace(R.id.eip_container, MemberSendFragment.newInstance());

                    fragmentTransaction.commit();

                }else if (id == R.id.navi_botto_time) {

                    FragmentManager fragmentManager = getSupportFragmentManager();

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.replace(R.id.eip_container, StartTimeFragment.newInstance());

                    fragmentTransaction.commit();

                }
                return false;
            }
        });


    }



}
