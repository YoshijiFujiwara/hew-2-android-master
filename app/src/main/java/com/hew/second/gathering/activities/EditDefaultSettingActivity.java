package com.hew.second.gathering.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.hew.second.gathering.fragments.EditDefaultSettingFragment;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.fragments.EditGroupFragment;

public class EditDefaultSettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_default);
        setTitle( "デフォルト追加" );

        // Backボタンを有効にする
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        if(savedInstanceState == null) {
            // FragmentManagerのインスタンス生成
            FragmentManager fragmentManager = getSupportFragmentManager();
            // FragmentTransactionのインスタンスを取得
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // インスタンスに対して張り付け方を指定する
            fragmentTransaction.replace(R.id.container, EditDefaultSettingFragment.newInstance(""));
            // 張り付けを実行
            fragmentTransaction.commit();
        }

    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try{
            if(getSupportFragmentManager().findFragmentById(R.id.container) instanceof EditDefaultSettingFragment){
                EditDefaultSettingFragment fragment = (EditDefaultSettingFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                fragment.removeFocus();
            }
        }catch (Exception e){
            Log.d("view", "フォーカスエラー：" + LogUtil.getLog() + e.toString());
        }
        return super.dispatchTouchEvent(ev);
    }
}
