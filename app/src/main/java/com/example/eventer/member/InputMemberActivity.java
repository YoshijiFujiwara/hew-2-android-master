package com.example.eventer.member;

import android.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class InputMemberActivity extends AppCompatActivity {

    private Long mId;
    private EditText mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_member);

        // Backボタンを有効にする
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mName = findViewById(R.id.name);

        if(getIntent() != null){
            mId = getIntent().getLongExtra("ID",-1);
            // TODO 既存データがあるなら取得

            mName.setText("既存データ");
        }

    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        // TODO データ保存
    }
}
