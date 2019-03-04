package com.hew.second.gathering.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.hotpepper.Shop;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class GuestSessionDetailActivity extends BaseActivity {

    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_session_detail);
        Shop shop = Parcels.unwrap(getIntent().getParcelableExtra("SHOP_DETAIL"));
        Session session = Parcels.unwrap(getIntent().getParcelableExtra("SESSION_DETAIL"));
        String status = getIntent().getStringExtra("STATUS");
        setTitle("セッション詳細");

        intent = new Intent();

        // Backボタンを有効にする
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        TextView title = findViewById(R.id.title);
        title.setText(session.name);

        TextView shopName = findViewById(R.id.shop_name);
        shopName.setText(shop.name);
        TextView time = findViewById(R.id.time);
        time.setText(session.start_time + "〜" + session.end_time);
        TextView countMember = findViewById(R.id.count_member);
        countMember.setText(session.users.size() + "人");

        TextView genre = findViewById(R.id.textView_genre);
        genre.setText(shop.genre.name);
        TextView address = findViewById(R.id.textView_address);
        address.setText(shop.address);
        TextView budget = findViewById(R.id.textView_budget);
        budget.setText(shop.budget.average);
        TextView access = findViewById(R.id.textView_access);
        access.setText(shop.mobile_access);
        TextView url = findViewById(R.id.textView_url);
        url.setText(shop.urls.pc);
        url.setTextColor(getResources().getColor(R.color.colorPrimary));

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

        Button allow = findViewById(R.id.button_allow);
        if(status.equals("WAIT")){
            allow.setOnClickListener((l) -> {
                // 参加する
                new MaterialDialog.Builder(this)
                        .title("ゲストイベント")
                        .content(session.name+ "に参加しますか？")
                        .positiveText("OK")
                        .onPositive((dialog, which) -> {
                            joinSession(session.id);
                        })
                        .negativeText("キャンセル")
                        .show();
            });
        } else {
            allow.setText("参加済みです");
            allow.setEnabled(false);
        }
        Button deny = findViewById(R.id.button_deny);
        deny.setOnClickListener((l) -> {
            // 参加しない
            new MaterialDialog.Builder(this)
                    .title("ゲストイベント")
                    .content(session.name+ "への参加を断りますか？")
                    .positiveText("OK")
                    .onPositive((dialog, which) -> {
                        denySession(session.id);
                    })
                    .negativeText("キャンセル")
                    .show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void joinSession(int sessionId) {

        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("join_status", "allow");
        Observable<SessionDetail> session = service.updateGuestSession(LoginUser.getToken(),sessionId, body);
        cd.add(session.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        (list) -> {
                            //遷移
                            intent.putExtra(SNACK_MESSAGE, "セッションに参加しました。");
                            setResult(RESULT_OK, intent);
                            finish();
                        },
                        (throwable) -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (!cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }));
    }

    private void denySession(int sessionId) {

        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("join_status", "deny");
        Observable<SessionDetail> session = service.updateGuestSession(LoginUser.getToken(),sessionId, body);
        cd.add(session.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        (list) -> {
                            //遷移
                            intent.putExtra(SNACK_MESSAGE, "セッションへの参加を断りました。");
                            setResult(RESULT_OK, intent);
                            finish();
                        },
                        (throwable) -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (!cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }));
    }
}
