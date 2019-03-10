package com.hew.second.gathering.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.AndroidRuntimeException;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;

public class ShowQrCodeActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr_code);
        setTitle("友達検索用QRコード");

        // Backボタンを有効にする
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        String data = LoginUser.getUniqueId();
        int size = 500;
        TextView uniqueId = findViewById(R.id.textView_unique_id);
        uniqueId.setText(data);

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


}
