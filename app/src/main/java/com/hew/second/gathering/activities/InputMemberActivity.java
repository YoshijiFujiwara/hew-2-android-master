package com.hew.second.gathering.activities;

import android.os.Bundle;
import android.widget.EditText;

import com.hew.second.gathering.R;

// TODO 仕組みわからん
public class InputMemberActivity extends BaseActivity {

    private EditText mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_member);
        setTitle( "友達追加" );

        // Backボタンを有効にする
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mName = findViewById(R.id.member_name);
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
