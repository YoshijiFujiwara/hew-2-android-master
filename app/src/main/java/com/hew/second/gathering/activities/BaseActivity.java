package com.hew.second.gathering.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.hew.second.gathering.api.Util;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseActivity extends AppCompatActivity {

    public static final String SNACK_MESSAGE = "MESSAGE";
    protected CompositeDisposable cd = new CompositeDisposable();
    public static final int INTENT_EDIT_GROUP = 1;
    public static final int INTENT_ADD_GROUP_MEMBER = 2;
    public static final int INTENT_EDIT_DEFAULT = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        Util.setLoading(false,this);
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
        String message = data.getStringExtra(SNACK_MESSAGE);
        if(message != null) {
            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
            snackbar.getView().setBackgroundColor(Color.BLACK);
            snackbar.setActionTextColor(Color.WHITE);
            snackbar.show();
        }
    }
}
