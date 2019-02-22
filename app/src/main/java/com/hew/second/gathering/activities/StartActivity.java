package com.hew.second.gathering.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.hew.second.gathering.R;

public class StartActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        ImageButton now_button = (ImageButton) findViewById(R.id.now_button);
        now_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //  今から飲むモードへ
                Intent intent = new Intent(getApplication(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        // 戻れない
    }
}
