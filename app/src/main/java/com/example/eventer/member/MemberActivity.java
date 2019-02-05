package com.example.eventer.member;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.eventer.member.api.ApiService;
import com.example.eventer.member.api.MemberData;
import com.example.eventer.member.api.MemberInfo;
import com.example.eventer.member.api.TokenInfo;
import com.example.eventer.member.api.Util;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MemberActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static String[] names = {
    };

    Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            final long[] newId = new long[1];

            @Override
            public void onClick(View view) {
                newId[0] = 0; // TODO 新規登録ID番号
                Intent intent = new Intent(MemberActivity.this, InputMemberActivity.class);
                intent.putExtra("ID", newId[0]);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // ListView生成
        ListView listView = findViewById(R.id.member_list);
        MemberAdapter adapter = new MemberAdapter(names);

        // ListViewにadapterをセット
        listView.setAdapter(adapter);
        retrofit = Util.getRetrofit();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // ログイン
        ApiService service = retrofit.create(ApiService.class);
        //Observable<TokenInfo> token = service.getToken("stepfootprint@gmail.com", "012345");
        Observable<TokenInfo> token = service.getRefreshToken(Util.getToken());

        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    Util.setToken(result.access_token);
                    return service.getMemberList(Util.getToken());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> updateList(list.data),  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー" + LogUtil.getLog() + throwable.toString());
                            //TODO:ログインページに飛ばす エラーメッセージとか表示も
                            Intent intent = new Intent(getApplication(), LoginActivity.class);
                            startActivity(intent);
                        }
                );
    }
    private void updateList(List<MemberInfo> data) {
        // ListView生成
        ListView listView = findViewById(R.id.member_list);
        ArrayList<String> ar = new ArrayList<String>();

        for (MemberInfo m : data) {
            ar.add(m.name);
        }
        String list[] = ar.toArray(new String[0]);
        MemberAdapter adapter = new MemberAdapter(list);
        // ListViewにadapterをセット
        listView.setAdapter(adapter);
    }

    private void finishRefresh(TokenInfo data) {
        // ListView生成
        ListView listView = findViewById(R.id.member_list);

        // テスト用
        String list[] = {
                data.access_token,
                data.token_type,
                Integer.toString(data.expires_in)
        };
        // トークン更新
        Util.setToken(data.access_token);
        MemberAdapter adapter = new MemberAdapter(list);
        // ListViewにadapterをセット
        listView.setAdapter(adapter);

        // 友達データ読み込み
        ApiService service = retrofit.create(ApiService.class);
        Observable<MemberData> member = service.getMemberList("Bearer "+ data.access_token);

        member.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        memberList -> updateList(memberList.data),
                        throwable -> Log.d("api", "API取得エラー:" + LogUtil.getLog() + ":" + throwable.toString())
                );

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.member, menu);
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

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
