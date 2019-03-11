package com.hew.second.gathering.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SearchView;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.Util;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class ShowQrCodeActivity extends BaseActivity {
    Handler mHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr_code);
        setTitle("友達検索用QRコード");
        mHandler = new Handler();

        // Backボタンを有効にする
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener((l) -> new IntentIntegrator(ShowQrCodeActivity.this).initiateScan());

        String data = LoginUser.getUniqueId(getSharedPreferences(Util.PREF_FILE_NAME, MODE_PRIVATE)) + ","
                + LoginUser.getUsername(getSharedPreferences(Util.PREF_FILE_NAME, MODE_PRIVATE));
        int size = 500;
        TextView uniqueId = findViewById(R.id.textView_unique_id);
        uniqueId.setText(LoginUser.getUniqueId(getSharedPreferences(Util.PREF_FILE_NAME, MODE_PRIVATE)));
        TextView username = findViewById(R.id.textView_username);
        username.setText(LoginUser.getUsername(getSharedPreferences(Util.PREF_FILE_NAME, MODE_PRIVATE)));

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

            HashMap hints = new HashMap();

            //文字コードの指定
            hints.put(EncodeHintType.CHARACTER_SET, "shiftjis");

            //誤り訂正レベルを指定
            //L 7%が復元可能
            //M 15%が復元可能
            //Q 25%が復元可能
            //H 30%が復元可能
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            Bitmap bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size, hints);

            ImageView imageViewQrCode = findViewById(R.id.imageView_qr_code);
            imageViewQrCode.setImageBitmap(bitmap);

        } catch (WriterException e) {
            throw new AndroidRuntimeException("Barcode Error.", e);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && data != null) {
            Log.d("readQR", result.getContents());
            String[] strUserData = result.getContents().split(",", 2);
            mHandler.post(() -> {
                new MaterialDialog.Builder(this)
                        .title(strUserData[1])
                        .content(strUserData[0])
                        .positiveText("友達申請")
                        .onPositive((dialog, which) -> {
                            requestFriendByUniqueId(strUserData[0]);
                        })
                        .negativeText("キャンセル")
                        .show();
            });
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void requestFriendByUniqueId(String uniqueId){
        dialog = new SpotsDialog.Builder().setContext(this).build();
        dialog.show();
        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("unique_id", uniqueId);
        Observable<Friend> token = service.requestAddFriend(body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        (list) -> {
                            dialog.dismiss();
                            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "友達申請を送信しました。", Snackbar.LENGTH_SHORT);
                            snackbar.getView().setBackgroundColor(Color.BLACK);
                            snackbar.setActionTextColor(Color.WHITE);
                            snackbar.show();
                        }, // 終了時
                        (throwable) -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            dialog.dismiss();
                            if (!cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 409 || ((HttpException) throwable).code() == 422)) {
                                    final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "すでに友達か、申請中です。", Snackbar.LENGTH_LONG);
                                    snackbar.getView().setBackgroundColor(Color.BLACK);
                                    snackbar.setActionTextColor(Color.WHITE);
                                    snackbar.show();
                                } else if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }));
    }
}
