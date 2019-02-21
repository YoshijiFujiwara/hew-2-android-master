package com.hew.second.gathering.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Profile;
import com.hew.second.gathering.api.ProfileDetail;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.fragments.BudgetFragment;
import com.hew.second.gathering.fragments.EditShopFragment;
import com.hew.second.gathering.fragments.EventFragment;
import com.hew.second.gathering.fragments.GroupFragment;
import com.hew.second.gathering.fragments.InviteFragment;
import com.hew.second.gathering.fragments.MemberFragment;
import com.hew.second.gathering.fragments.SessionFragment;
import com.hew.second.gathering.fragments.SessionMainFragment;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


//import com.hew.second.gathering.fragments.EventFragment;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ApiService service = Util.getService();
        Observable<ProfileDetail> profile = service.getProfile(LoginUser.getToken());
        cd.add(profile.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            profile(list.data);
                            if (savedInstanceState == null) {
                                // FragmentManagerのインスタンス生成
                                FragmentManager fragmentManager = getSupportFragmentManager();
                                // FragmentTransactionのインスタンスを取得
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                // インスタンスに対して張り付け方を指定する
                                fragmentTransaction.replace(R.id.container, EventFragment.newInstance());
                                // 張り付けを実行
                                fragmentTransaction.commit();
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                        }
                ));


    }

    private void profile(Profile data) {
        //ユーザー情報表示
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        TextView user_name = (TextView) header.findViewById(R.id.user_name);
        TextView user_email = (TextView) header.findViewById(R.id.user_email);
        user_name.setText(data.username);
        user_email.setText(data.email);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof MemberFragment) {
                MemberFragment fragment = (MemberFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                fragment.removeFocus();
            }
            if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof GroupFragment) {
                GroupFragment fragment = (GroupFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                fragment.removeFocus();
            }
            if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof EditShopFragment) {
                EditShopFragment fragment = (EditShopFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                fragment.removeFocus();
            }
        } catch (Exception e) {
            Log.d("view", "フォーカスエラー：" + LogUtil.getLog() + e.toString());
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_top) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container, EventFragment.newInstance());
                fragmentTransaction.commit();
            }
        }
// else if (id == R.id.nav_group) {
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            if(fragmentManager != null){
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.addToBackStack(null);
//                fragmentTransaction.replace(R.id.container, GroupFragment.newInstance());
//                fragmentTransaction.commit();
//            }
//        }
        else if (id == R.id.nav_member) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container, MemberFragment.newInstance());
                fragmentTransaction.commit();
            }

        } else if (id == R.id.nav_session) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container, SessionFragment.newInstance());
                fragmentTransaction.commit();
            }

        } else if (id == R.id.nav_logout) {
            // ログイン情報初期化
            LoginUser.deleteUserInfo(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE));
            // ログイン画面へ
            Intent intent = new Intent(getApplication(), LoginActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_budget) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container, BudgetFragment.newInstance());
                fragmentTransaction.commit();
            }

        } else if (id == R.id.nav_session_main) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if(fragmentManager != null){
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container, SessionMainFragment.newInstance());
                fragmentTransaction.commit();
            }

        } else if (id == R.id.nav_finish) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container, InviteFragment.newInstance());
                fragmentTransaction.commit();
            }

        }
        else if (id == R.id.nav_session_process){

        }
//        else if (id == R.id.nav_default){
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            if(fragmentManager != null){
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.addToBackStack(null);
//                fragmentTransaction.replace(R.id.container, DefaultSettingFragment.newInstance());
//                fragmentTransaction.commit();
//            }
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // fragmentからの呼びだしの場合
        switch (requestCode & 0xffff) {
            //店検索から戻ってきた場合
            case (INTENT_SHOP_DETAIL):
                if (resultCode == RESULT_OK) {
                    mHandler.post(() -> {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        if (fragmentManager != null) {
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.replace(R.id.container, SessionMainFragment.newInstance());
                            fragmentTransaction.commit();
                        }

                    });
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
