package com.hew.second.gathering.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.hew.second.gathering.api.Util;

import icepick.Icepick;
import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseActivity extends AppCompatActivity {

    public static final String SNACK_MESSAGE = "MESSAGE";
    protected CompositeDisposable cd = new CompositeDisposable();
    public static final int INTENT_EDIT_GROUP = 1;
    public static final int INTENT_ADD_GROUP_MEMBER = 2;
    public static final int INTENT_EDIT_DEFAULT = 3;
    public static final int INTENT_SHOP_DETAIL = 3;
    public static final int INTENT_GUEST_SESSION_DETAIL = 4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        Intent i = getIntent();
        String message = i.getStringExtra(SNACK_MESSAGE);
        if(message != null) {
            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(Color.BLACK);
            snackbar.setActionTextColor(Color.WHITE);
            snackbar.show();
        }
    }
    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    protected void onStop(){
        cd.clear();
        super.onStop();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
            String message = data.getStringExtra(SNACK_MESSAGE);
            if(message != null) {
                final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(Color.BLACK);
                snackbar.setActionTextColor(Color.WHITE);
                snackbar.show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }
}
