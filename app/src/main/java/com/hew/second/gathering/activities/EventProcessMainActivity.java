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
import com.hew.second.gathering.fragments.ReservationPhoneFragment;
import com.hew.second.gathering.fragments.StartTimeFragment;

public class EventProcessMainActivity extends AppCompatActivity {
//セッション詳細画面
//    activity_event_process_main.xml
//      |→FrameLayout(id :eip_parent_container)
//      | include → event_in_process.xml(FrameLayout id :eip_container)
//      BottomNavigationの挙動が停止状態で各画面（Fragment」）呼び出し可能
//        |→ViewPager＋TabLayout＋Fragmentを呼び出すはアプリが落ちる？？？
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_process_main);

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
//                      予算計算画面呼び出し予定
//                    fragmentTransaction.replace(R.id.event_process_container, BudgetFragment.newInstance());

                    fragmentTransaction.commit();

                }else if (id == R.id.navi_botto_reservation) {
                    FragmentManager fragmentManager = getSupportFragmentManager();

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.replace(R.id.eip_container, ReservationPhoneFragment.newInstance());

                    fragmentTransaction.commit();

                }else if (id == R.id.navi_botto_member) {
                    FragmentManager fragmentManager = getSupportFragmentManager();

                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                      Memberへ招待送信　打ち切り画面
//                    fragmentTransaction.replace(R.id.eip_container, InviteFragment.newInstance());

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
