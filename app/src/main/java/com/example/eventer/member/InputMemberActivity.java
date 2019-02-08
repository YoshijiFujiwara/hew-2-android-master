package com.example.eventer.member;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

// TODO 仕組みわからん
public class InputMemberActivity extends AppCompatActivity {

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

        mName = findViewById(R.id.group_name);
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
