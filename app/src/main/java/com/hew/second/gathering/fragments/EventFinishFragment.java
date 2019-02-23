package com.hew.second.gathering.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hew.second.gathering.R;

public class EventFinishFragment extends BaseFragment {

    // TODO:画面全般組む

    public EventFinishFragment() {
    }


    public static EventFinishFragment newInstance() {
        return new EventFinishFragment();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_event_finish,container,false);

        return view;
    }
}
