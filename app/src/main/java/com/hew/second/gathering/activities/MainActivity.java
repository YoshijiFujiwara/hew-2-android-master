package com.hew.second.gathering.activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Profile;
import com.hew.second.gathering.api.ProfileDetail;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.fragments.AttributeFragment;
import com.hew.second.gathering.fragments.DefaultSettingFragment;
import com.hew.second.gathering.fragments.EditShopFragment;
import com.hew.second.gathering.fragments.FriendFragment;
import com.hew.second.gathering.fragments.GroupFragment;
import com.hew.second.gathering.fragments.MemberFragment;
import com.hew.second.gathering.fragments.SessionFragment;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;


public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Handler mHandler;
    public boolean requestUpdateFriend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener(){
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_SETTLING) {
                    if (!drawer.isDrawerOpen(R.id.drawer_layout)) {
                        if(LoginUser.getUsername(getSharedPreferences(Util.PREF_FILE_NAME,MODE_PRIVATE)).isEmpty()){
                            updateProfile();
                        }
                    }
                }
            }
        });
        setProfile();
        updateProfile();

        View header = navigationView.getHeaderView(0);
        FloatingActionButton logo = header.findViewById(R.id.fab_qr);
        logo.setOnClickListener((l)->{
            Intent intent = new  Intent(getApplication(),ShowQrCodeActivity.class);
            startActivity(intent);
        });

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            if (bundle != null) {
                // 投げられた値で初期画面分岐
                String fragment = bundle.getString("FRAGMENT");
                if (fragment == null) {
                    fragmentTransaction.replace(R.id.container, SessionFragment.newInstance());
                } else if (fragment.equals("SESSION")) {
                    fragmentTransaction.replace(R.id.container, SessionFragment.newInstance());
                } else if (fragment.equals("FRIEND")) {
                    fragmentTransaction.replace(R.id.container, MemberFragment.newInstance());
                } else if (fragment.equals("GROUP")) {
                    fragmentTransaction.replace(R.id.container, GroupFragment.newInstance());
                } else if (fragment.equals("ATTRIBUTE")) {
                    fragmentTransaction.replace(R.id.container, AttributeFragment.newInstance());
                } else if (fragment.equals("DEFAULT")) {
                    fragmentTransaction.replace(R.id.container, DefaultSettingFragment.newInstance());
                } else {
                    fragmentTransaction.replace(R.id.container, SessionFragment.newInstance());
                }
            } else {
                fragmentTransaction.replace(R.id.container, SessionFragment.newInstance());
            }
            fragmentTransaction.commit();
        }
    }

    //      試しにonResume
    @Override
    protected void onResume() {
        super.onResume();
        setProfile();
    }

    private void setProfile() {
        if (!LoginUser.getUsername(getSharedPreferences(Util.PREF_FILE_NAME,MODE_PRIVATE)).isEmpty()) {
            NavigationView navigationView = findViewById(R.id.nav_view);
            View header = navigationView.getHeaderView(0);
            TextView user_name = header.findViewById(R.id.user_name);
            TextView user_email = header.findViewById(R.id.user_email);
            TextView uniqueId = header.findViewById(R.id.user_unique_id);
            user_name.setText(LoginUser.getUsername(getSharedPreferences(Util.PREF_FILE_NAME,MODE_PRIVATE)));
            user_email.setText(LoginUser.getEmail(getSharedPreferences(Util.PREF_FILE_NAME, MODE_PRIVATE)));
            uniqueId.setText("@" + LoginUser.getUniqueId(getSharedPreferences(Util.PREF_FILE_NAME, MODE_PRIVATE)));
        }
    }
    private void updateProfile(){
        ApiService service = Util.getService();
        Observable<ProfileDetail> profile = service.getProfile();
        cd.add(profile.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            profile(list.data);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                Intent intent = new Intent(getApplication(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                ));
    }

    private void profile(Profile data) {
        //ユーザー情報表示
        NavigationView navigationView = findViewById(R.id.nav_view);
        View header = navigationView.getHeaderView(0);
        TextView user_name = header.findViewById(R.id.user_name);
        TextView user_email = header.findViewById(R.id.user_email);
        user_name.setText(data.username);
        user_email.setText(data.email);
        TextView uniqueId = header.findViewById(R.id.user_unique_id);
        uniqueId.setText("@" + data.unique_id);

        LoginUser.setEmail(getSharedPreferences(Util.PREF_FILE_NAME, MODE_PRIVATE), data.email);
        LoginUser.setUniqueId(getSharedPreferences(Util.PREF_FILE_NAME, MODE_PRIVATE),data.unique_id);
        LoginUser.setUsername(getSharedPreferences(Util.PREF_FILE_NAME,MODE_PRIVATE),data.username);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof MemberFragment) {
                MemberFragment fragment = (MemberFragment) getSupportFragmentManager().findFragmentById(R.id.container);
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
            Intent intent = new Intent(getApplication(), EditProfileActivity.class);
            startActivityForResult(intent, INTENT_PROFILE);
        } else if (id == R.id.action_logout) {
            // ログイン情報初期化
            //LoginUser.deleteUserInfo(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE));
            // ログイン画面へ
            Intent intent = new Intent(getApplication(), LoginActivity.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_top) {
            Intent intent = new Intent(getApplication(), StartActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_new) {
            Intent intent = new Intent(getApplication(), EventProcessMainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("FRAGMENT", "DEFAULT");
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (id == R.id.nav_session) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container, SessionFragment.newInstance());
                fragmentTransaction.commit();
            }
        } else if (id == R.id.nav_guest) {
            Intent intent = new Intent(getApplication(), GuestActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_friend) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container, MemberFragment.newInstance());
                fragmentTransaction.commit();
            }

        } else if (id == R.id.nav_group) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container, GroupFragment.newInstance());
                fragmentTransaction.commit();
            }

        } else if (id == R.id.nav_attribute) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container, AttributeFragment.newInstance());
                fragmentTransaction.commit();
            }
        } else if (id == R.id.nav_default) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.replace(R.id.container, DefaultSettingFragment.newInstance());
                fragmentTransaction.commit();
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // fragmentからの呼びだしの場合

        switch (requestCode & 0xffff) {
            //店検索から戻ってきた場合
            case (INTENT_ATTRIBUTE_DETAIL):
                if (! (getSupportFragmentManager().findFragmentById(R.id.container) instanceof AttributeFragment)) {
                    mHandler.post(() -> {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        if (fragmentManager != null) {
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.replace(R.id.container, AttributeFragment.newInstance());
                            fragmentTransaction.commit();
                        }
                    });
                }
                break;
            case INTENT_EDIT_DEFAULT:
                if (! (getSupportFragmentManager().findFragmentById(R.id.container) instanceof DefaultSettingFragment)) {
                    mHandler.post(() -> {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        if (fragmentManager != null) {
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.replace(R.id.container, DefaultSettingFragment.newInstance());
                            fragmentTransaction.commit();
                        }

                    });
                }
            default:
                break;
        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}
