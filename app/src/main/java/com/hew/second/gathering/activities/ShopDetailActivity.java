package com.hew.second.gathering.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.SelectedSession;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.hotpepper.Shop;
import com.hew.second.gathering.views.adapters.MemberAdapter;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class ShopDetailActivity extends BaseActivity {

    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);
        Shop shop = Parcels.unwrap(getIntent().getParcelableExtra("SHOP_DETAIL"));
        setTitle("店舗詳細");

        intent = new Intent();

        // Backボタンを有効にする
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        TextView shopName = findViewById(R.id.textView_shopName);
        TextView shopNameKana = findViewById(R.id.textView_shopName_kana);
        TextView genre = findViewById(R.id.textView_genre);
        TextView address = findViewById(R.id.textView_address);
        TextView url = findViewById(R.id.textView_url);
        url.setText(shop.urls.pc);
        TextView budget = findViewById(R.id.textView_budget);
        TextView access = findViewById(R.id.textView_access);
        TextView open = findViewById(R.id.textView_open);
        TextView close = findViewById(R.id.textView_close);

        ImageView imageView = findViewById(R.id.imageView_shop);
        Picasso.get()
                .load(shop.photo.pc.l)
                .fit()
                .centerInside()
                .into(imageView);

        url.setOnClickListener((l) -> {
            final CustomTabsIntent tabsIntent = new CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setToolbarColor(ContextCompat.getColor(getApplication(), R.color.colorPrimary))
                    .setStartAnimations(getApplication(), R.anim.slide_in_right, R.anim.slide_out_left)
                    .setExitAnimations(getApplication(), android.R.anim.slide_in_left, android.R.anim.slide_out_right).build();

            Uri uri = Uri.parse(url.getText().toString());
            tabsIntent.launchUrl(this, uri);
        });

        Button submit = findViewById(R.id.button_submit);
        submit.setOnClickListener((l) -> {
            int id = SelectedSession.getSharedSessionId(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE));
            if (id != -1) {
                updateSession(id, shop.id);
            } else {
                createSession(shop.id);
            }
        });
        Button back = findViewById(R.id.button_back);
        back.setOnClickListener((l) -> {
            onBackPressed();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void createSession(String shop_id) {

        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("name", "新規セッション");
        body.put("shop_id", shop_id);
        Observable<SessionDetail> session = service.createSession(LoginUser.getToken(), body);
        cd.add(session.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        (list) -> {
                            //セッションID格納
                            SelectedSession.setSessionId(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE), list.data.id);
                            SelectedSession.setSessionDetail(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE), list.data);
                            //遷移
                            intent.putExtra(SNACK_MESSAGE, "セッションを作成しました。");
                            setResult(RESULT_OK, intent);
                            finish();

                        },
                        (throwable) -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (!cd.isDisposed()) {
                                if (throwable instanceof HttpException && ((HttpException) throwable).code() == 401) {
                                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }));
    }

    private void updateSession(int id, String shop_id) {
        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("shop_id", shop_id);
        Observable<SessionDetail> session = service.updateSession(LoginUser.getToken(),id, body);
        cd.add(session.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        (list) -> {
                            //セッションID格納
                            SelectedSession.setSessionId(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE), list.data.id);
                            SelectedSession.setSessionDetail(getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE), list.data);
                            //遷移
                            intent.putExtra(SNACK_MESSAGE, "セッションを更新しました。");
                            setResult(RESULT_OK, intent);
                            finish();

                        },
                        (throwable) -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (!cd.isDisposed()) {
                                if (throwable instanceof HttpException && ((HttpException) throwable).code() == 401) {
                                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }));

    }
}
