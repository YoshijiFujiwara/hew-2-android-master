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
import com.hew.second.gathering.api.SessionUser;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.hotpepper.Shop;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

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
        setTitle("イベント詳細");

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
        TextView time = findViewById(R.id.time);// 時刻表示フォーマット
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat output = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        SimpleDateFormat outputShort = new SimpleDateFormat("HH:mm", Locale.getDefault());
        StringBuilder strTime = new StringBuilder();
        if(session.start_time != null){
            try{
                Date start = sdf.parse(session.start_time);
                strTime.append(output.format(start));
                strTime.append(" 〜 ");
                if(session.end_time != null) {
                    Date end = sdf.parse(session.end_time);
                    if(dateOnly.format(start).equals(dateOnly.format(end))){
                        strTime.append(outputShort.format(end));
                    }else{
                        strTime.append(output.format(end));
                    }
                }else{
                    strTime.append("未定");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            strTime.append("未定");
        }
        time.setText(strTime.toString());

        TextView countMember = findViewById(R.id.count_member);
        int ok = 0;
        for (SessionUser u : session.users) {
            if (u.join_status.equals("allow")) {
                ok++;
            }
        }
        countMember.setText(Integer.toString(ok + 1) + " / " + (session.users.size() + 1) + "人");

        TextView kanji = findViewById(R.id.textView_kanji);
        kanji.setText(session.manager.username);
        TextView kanjiId = findViewById(R.id.textView_kanji_id);
        kanjiId.setText("@" + session.manager.unique_id);
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

        address.setOnClickListener((l) -> {
            Uri uri = Uri.parse("geo:<" + shop.lat + ">,<" + shop.lng + ">?q="+ shop.name);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
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
        Observable<SessionDetail> session = service.updateGuestSession(sessionId, body);
        cd.add(session.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        (list) -> {
                            //遷移
                            intent.putExtra(SNACK_MESSAGE, "イベントに参加しました。");
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
        Observable<SessionDetail> session = service.updateGuestSession(sessionId, body);
        cd.add(session.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        (list) -> {
                            //遷移
                            intent.putExtra(SNACK_MESSAGE, "イベントへの参加を断りました。");
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
