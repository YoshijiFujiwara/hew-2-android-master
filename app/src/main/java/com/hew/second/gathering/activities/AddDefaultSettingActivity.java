package com.hew.second.gathering.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.R;
import com.hew.second.gathering.fragments.AddDefaultSettingFragment;
import com.hew.second.gathering.fragments.EditDefaultSettingFragment;

public class AddDefaultSettingActivity extends BaseActivity {

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
            fragmentTransaction.replace(R.id.container, AddDefaultSettingFragment.newInstance(""));
            // 張り付けを実行
            fragmentTransaction.commit();
        }

    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try{
            if(getSupportFragmentManager().findFragmentById(R.id.container) instanceof AddDefaultSettingFragment){
                AddDefaultSettingFragment fragment = (AddDefaultSettingFragment) getSupportFragmentManager().findFragmentById(R.id.container);
                fragment.removeFocus();
            }
        }catch (Exception e){
            Log.d("view", "フォーカスエラー：" + LogUtil.getLog() + e.toString());
        }
        return super.dispatchTouchEvent(ev);
    }

//    @Override
//    public boolean onSupportNavigateUp(){
//        onBackPressed();
//        return true;
//    }
//
//    @Override
//    public void onBackPressed(){
//        // データ保存
//        Util.setLoading(true,this);
//        try{
//            if(getSupportFragmentManager().findFragmentById(R.id.container) instanceof EditDefaultSettingFragment){
//                @NonNull EditDefaultSettingFragment fragment = (EditDefaultSettingFragment) getSupportFragmentManager().findFragmentById(R.id.container);
//                fragment.saveDefaultSettingName();
//            }
//        }catch (Exception e){
//            Log.d("view", "フォーカスエラー：" + LogUtil.getLog() + e.toString());
//            Util.setLoading(false,this);
//            Intent intent = new Intent();
//            intent.putExtra(SNACK_MESSAGE,"更新に失敗しました。");
//            setResult(RESULT_OK, intent);
//            finish();
//        }
//        super.onBackPressed();
//    }

}
