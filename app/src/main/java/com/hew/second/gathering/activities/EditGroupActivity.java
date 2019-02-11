package com.hew.second.gathering.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;

import com.hew.second.gathering.fragments.EditGroupFragment;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Util;

public class EditGroupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);
        setTitle( "友達追加" );

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
            fragmentTransaction.replace(R.id.container, EditGroupFragment.newInstance(""));
            // 張り付けを実行
            fragmentTransaction.commit();
        }
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try{
            if(getSupportFragmentManager().findFragmentById(R.id.container) instanceof EditGroupFragment){
                EditGroupFragment fragment = (EditGroupFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                fragment.removeFocus();
            }
        }catch (Exception e){
            Log.d("view", "フォーカスエラー：" + LogUtil.getLog() + e.toString());
        }
        return super.dispatchTouchEvent(ev);
    }
    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed(){
        // TODO データ保存
        Util.setLoading(true,this);
        try{
            if(getSupportFragmentManager().findFragmentById(R.id.container) instanceof EditGroupFragment){
                @NonNull EditGroupFragment fragment = (EditGroupFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                fragment.saveGroupName();
            }
        }catch (Exception e){
            Log.d("view", "フォーカスエラー：" + LogUtil.getLog() + e.toString());
            Util.setLoading(false,this);
            Intent intent = new Intent();
            intent.putExtra(SNACK_MESSAGE,"更新に失敗しました。");
            setResult(RESULT_OK, intent);
            finish();
        }
        //super.onBackPressed();
    }

}
