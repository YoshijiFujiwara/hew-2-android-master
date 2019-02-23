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

public class ShopDetailActivity extends BaseActivity {

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);
        Shop shop = Parcels.unwrap(getIntent().getParcelableExtra("SHOP_DETAIL"));
        Session session = Parcels.unwrap(getIntent().getParcelableExtra("SESSION_DETAIL"));
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
            if (session != null) {
                updateSession(session, shop);
            } else {
                createSession(shop);
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

    private void createSession(Shop shop) {

        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("name", "新規セッション");
        body.put("shop_id", shop.id);
        Observable<SessionDetail> session = service.createSession(LoginUser.getToken(), body);
        cd.add(session.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        (list) -> {
                            //遷移
                            intent.putExtra(SNACK_MESSAGE, "セッションを作成しました。");
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
                        }));
    }

    private void updateSession(Session session, Shop shop) {
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
                            intent.putExtra(SNACK_MESSAGE, "セッションを更新しました。");
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
                        }));

    }
}
