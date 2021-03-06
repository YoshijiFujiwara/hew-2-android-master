package com.hew.second.gathering.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
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
import com.hew.second.gathering.api.DefaultSetting;
import com.hew.second.gathering.api.Profile;
import com.hew.second.gathering.api.ProfileDetail;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.fragments.ApplyDefaultFragment;
import com.hew.second.gathering.fragments.BudgetFragment;
import com.hew.second.gathering.fragments.EditShopFragment;
import com.hew.second.gathering.fragments.EventFinishFragment;
import com.hew.second.gathering.fragments.InviteFragment;
import com.hew.second.gathering.fragments.ReservationPhoneFragment;
import com.hew.second.gathering.fragments.StartTimeFragment;
import com.hew.second.gathering.hotpepper.GourmetResult;
import com.hew.second.gathering.hotpepper.HpApiService;
import com.hew.second.gathering.hotpepper.HpHttp;
import com.hew.second.gathering.hotpepper.Shop;

import org.parceler.Parcels;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import icepick.State;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class EventProcessMainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    //セッション詳細画面
//    activity_event_process_main.xml
//      |→FrameLayout(id :eip_parent_container)
//      | include → event_in_process.xml(FrameLayout id :eip_container)
//      BottomNavigationの挙動が停止状態で各画面（Fragment」）呼び出し可能
//        |→ViewPager＋TabLayout＋Fragmentを呼び出すはアプリが落ちる？？？

    Handler mHandler = null;
    @State
    public Session session = null;
    @State
    public Shop shop = null;
    @State
    public String fragment = null;
    @State
    public DefaultSetting defaultSetting = null;

    public boolean requestUpdateInvited = false;
    public boolean requestUpdateInviteOne = false;
    public boolean requestUpdateInviteGroup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_process_main);

        mHandler = new Handler();
        Toolbar toolbar = (Toolbar) findViewById(R.id.sip_toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_event);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_event);
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

        BottomNavigationView bnv = findViewById(R.id.eip_bottom_navigation);
        //ボトムバー選択時
        bnv.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.navi_boto_main) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.eip_container, EventFinishFragment.newInstance());
                    fragmentTransaction.commit();

                } else if (id == R.id.navi_botto_budget) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.eip_container, BudgetFragment.newInstance());
                    fragmentTransaction.commit();

                } else if (id == R.id.navi_botto_reservation) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.eip_container, ReservationPhoneFragment.newInstance());
                    fragmentTransaction.commit();

                } else if (id == R.id.navi_botto_member) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.eip_container, InviteFragment.newInstance());
                    fragmentTransaction.commit();

                } else if (id == R.id.navi_botto_time) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.eip_container, StartTimeFragment.newInstance());
                    fragmentTransaction.commit();
                }
                return true;
            }
        });


        // 遷移周り
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            // セッション情報取得
            this.session = Parcels.unwrap(getIntent().getParcelableExtra("SESSION_DETAIL"));
            this.shop = Parcels.unwrap(getIntent().getParcelableExtra("SHOP_DETAIL"));
            // 投げられた値で初期画面分岐
            fragment = bundle.getString("FRAGMENT");
        }

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            bnv.setSelectedItemId(R.id.navi_boto_main);
            if (fragment == null) {
                fragmentTransaction.replace(R.id.eip_container, EventFinishFragment.newInstance());
            } else if (fragment.equals("SHOP")) {
                fragmentTransaction.replace(R.id.eip_container, EditShopFragment.newInstance());
            } else if (fragment.equals("DEFAULT")) {
                fragmentTransaction.replace(R.id.eip_container, ApplyDefaultFragment.newInstance());
            } else if (fragment.equals("TIME")) {
                bnv.setSelectedItemId(R.id.navi_botto_time);
                fragmentTransaction.replace(R.id.eip_container, StartTimeFragment.newInstance());
            } else if (fragment.equals("RESERVE")) {
                bnv.setSelectedItemId(R.id.navi_botto_reservation);
                fragmentTransaction.replace(R.id.eip_container, ReservationPhoneFragment.newInstance());
            } else if (fragment.equals("BUDGET")) {
                bnv.setSelectedItemId(R.id.navi_botto_budget);
                fragmentTransaction.replace(R.id.eip_container, BudgetFragment.newInstance());
            } else {
                fragmentTransaction.replace(R.id.eip_container, EventFinishFragment.newInstance());
            }
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setProfile();
        // セッション未作成の場合非表示
        BottomNavigationView bnv = findViewById(R.id.eip_bottom_navigation);
        if (session == null) {
            bnv.setVisibility(View.GONE);
        } else {
            if (this.shop == null) {
                fetchShop();
            }
            bnv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // fragmentからの呼びだしの場合
        switch (requestCode & 0xffff) {
            //店検索から戻ってきた場合
            case (INTENT_SHOP_DETAIL):
                if (resultCode == RESULT_OK) {
                    if (!(getSupportFragmentManager().findFragmentById(R.id.eip_container) instanceof EventFinishFragment)) {
                        mHandler.post(() -> {
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            if (fragmentManager != null) {
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                //fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.replace(R.id.eip_container, EventFinishFragment.newInstance());
                                fragmentTransaction.commit();
                            }
                        });
                    }
                    this.session = Parcels.unwrap(data.getParcelableExtra("SESSION_DETAIL"));
                    this.shop = Parcels.unwrap(data.getParcelableExtra("SHOP_DETAIL"));
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

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
            Intent intent = new Intent(getApplication(), MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("FRAGMENT", "SESSION");
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (id == R.id.nav_guest) {
            Intent intent = new Intent(getApplication(), GuestActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_friend) {
            Intent intent = new Intent(getApplication(), MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("FRAGMENT", "FRIEND");
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (id == R.id.nav_group) {
            Intent intent = new Intent(getApplication(), MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("FRAGMENT", "GROUP");
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (id == R.id.nav_attribute) {
            Intent intent = new Intent(getApplication(), MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("FRAGMENT", "ATTRIBUTE");
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (id == R.id.nav_default) {
            Intent intent = new Intent(getApplication(), MainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("FRAGMENT", "DEFAULT");
            intent.putExtras(bundle);
            startActivity(intent);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_event);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            if (getSupportFragmentManager().findFragmentById(R.id.eip_container) instanceof InviteFragment) {
                InviteFragment fragment = (InviteFragment) getSupportFragmentManager().findFragmentById(R.id.eip_container);
                fragment.removeFocus();
            }
            if (getSupportFragmentManager().findFragmentById(R.id.eip_container) instanceof ApplyDefaultFragment) {
                ApplyDefaultFragment fragment = (ApplyDefaultFragment) getSupportFragmentManager().findFragmentById(R.id.eip_container);
                fragment.removeFocus();
            }

        } catch (Exception e) {
            Log.d("view", "フォーカスエラー：" + LogUtil.getLog() + e.toString());
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_event);
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

    private void setProfile() {
        if (!LoginUser.getUsername(getSharedPreferences(Util.PREF_FILE_NAME,MODE_PRIVATE)).isEmpty()) {
            NavigationView navigationView = findViewById(R.id.nav_view_event);
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
        NavigationView navigationView = findViewById(R.id.nav_view_event);
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

    private void fetchShop() {
        if (session.shop_id != null) {
            dialog = new SpotsDialog.Builder().setContext(this).build();
            dialog.show();
            HpApiService hpService = HpHttp.getService();
            Map<String, String> body = new HashMap<>();
            body.put("id", session.shop_id);
            Observable<GourmetResult> shopList = hpService.getShopList(body);
            cd.add(shopList
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe(
                            list -> {
                                this.shop = list.results.shop.get(0);
                                dialog.dismiss();
                            },  // 成功時
                            throwable -> {
                                Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                                if (!cd.isDisposed()) {
                                    if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                        // ログインアクティビティへ遷移
                                        Intent intent = new Intent(getApplication(), LoginActivity.class);
                                        startActivity(intent);
                                    }
                                    dialog.dismiss();
                                }
                            }
                    ));
        }
    }

}
