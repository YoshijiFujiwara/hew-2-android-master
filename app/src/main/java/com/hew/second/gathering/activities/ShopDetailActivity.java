package com.hew.second.gathering.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.DefaultSetting;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.SessionUser;
import com.hew.second.gathering.api.SessionUserList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.hotpepper.Shop;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class ShopDetailActivity extends BaseActivity {

    Intent intent;
    DefaultSetting defaultSetting = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);
        Shop shop = Parcels.unwrap(getIntent().getParcelableExtra("SHOP_DETAIL"));
        Session session = Parcels.unwrap(getIntent().getParcelableExtra("SESSION_DETAIL"));
        defaultSetting = Parcels.unwrap(getIntent().getParcelableExtra("DEFAULT_DETAIL"));
        setTitle("店舗詳細");

        intent = new Intent();

        // Backボタンを有効にする
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        TextView shopName = findViewById(R.id.textView_shopName);
        shopName.setText(shop.name);
        TextView shopNameKana = findViewById(R.id.textView_shopName_kana);
        shopNameKana.setText(shop.name_kana);
        TextView genre = findViewById(R.id.textView_genre);
        genre.setText(shop.genre.name);
        TextView address = findViewById(R.id.textView_address);
        address.setText(shop.address);
        TextView url = findViewById(R.id.textView_url);
        url.setText(shop.urls.pc);
        TextView budget = findViewById(R.id.textView_budget);
        budget.setText(shop.budget.average);
        TextView access = findViewById(R.id.textView_access);
        access.setText(shop.mobile_access);
        TextView open = findViewById(R.id.textView_open);
        open.setText(shop.open);
        TextView close = findViewById(R.id.textView_close);
        close.setText(shop.close);

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
            if (session != null) {
                updateSession(session, shop);
            } else {
                new MaterialDialog.Builder(this)
                        .title("イベント作成")
                        .content("タイトル")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .inputRangeRes(1, 30, R.color.colorAccentDark)
                        .input("ヒント", "新規イベント", (MaterialDialog dialog, CharSequence input) -> {
                            createSession(input.toString(), shop);
                        })
                        .negativeText("キャンセル")
                        .show();
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

    private void createSession(String title, Shop shop) {

        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();

        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("name", title);
        body.put("shop_id", shop.id);
        if(defaultSetting == null) {
            Observable<SessionDetail> session = service.createSession(LoginUser.getToken(), body);
            cd.add(session.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe(
                            (list) -> {
                                //遷移
                                dialog.dismiss();
                                intent.putExtra(SNACK_MESSAGE, "イベントを作成しました。");
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("SHOP_DETAIL", Parcels.wrap(shop));
                                bundle.putParcelable("SESSION_DETAIL", Parcels.wrap(list.data));
                                intent.putExtras(bundle);
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
                                dialog.dismiss();
                            }));
        } else {
            // Date型のフォーマットの設定
            SimpleDateFormat sdf = new SimpleDateFormat("dd:HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long date = 0;
            long now = new Date().getTime();
            try {
                date = sdf.parse(defaultSetting.timer).getTime();
            } catch(ParseException e) {
                e.printStackTrace();
            }
            now += date;
            // 日時を加算する
            Date start = new Date();
            start.setTime(now);
            body.put("start_time", sdf2.format(start));
            Observable<SessionDetail> session = service.createSession(LoginUser.getToken(), body);
            Bundle bundle = new Bundle();
            final Session[] s = new Session[1];

            cd.add(session.subscribeOn(Schedulers.io())
                    .flatMap((v)->{
                        s[0] = v.data;
                        return service.createSessionGroup(LoginUser.getToken(),v.data.id,defaultSetting.group.id);
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe(
                            (list) -> {
                                //遷移
                                dialog.dismiss();
                                s[0].users = new ArrayList<>(list.data);
                                intent.putExtra(SNACK_MESSAGE, "イベントを作成しました。");
                                bundle.putParcelable("SESSION_DETAIL", Parcels.wrap(s[0]));
                                bundle.putParcelable("SHOP_DETAIL", Parcels.wrap(shop));
                                intent.putExtras(bundle);
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
                                dialog.dismiss();
                            }));
        }
    }

    private void updateSession(Session session, Shop shop) {
        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();

        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("shop_id", shop.id);
        Observable<SessionDetail> sessionDetail = service.updateSession(LoginUser.getToken(), session.id, body);
        cd.add(sessionDetail.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        (list) -> {
                            //遷移
                            dialog.dismiss();
                            intent.putExtra(SNACK_MESSAGE, "イベントを更新しました。");
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("SHOP_DETAIL", Parcels.wrap(shop));
                            bundle.putParcelable("SESSION_DETAIL", Parcels.wrap(list.data));
                            intent.putExtras(bundle);
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
                            dialog.dismiss();
                        }));

    }
}
