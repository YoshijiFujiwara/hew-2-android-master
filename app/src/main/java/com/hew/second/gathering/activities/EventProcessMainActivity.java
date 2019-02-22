package com.hew.second.gathering.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Session;

import org.parceler.Parcels;

import java.util.List;

public class EventProcessMainActivity extends AppCompatActivity {
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.id.event_process_main);

        List<Session> sessions = Parcels.unwrap(getIntent().getParcelableExtra("sessions"));
    }
}
