package com.hew.second.gathering.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hew.second.gathering.R;

public class FragmentEventPrecessMain extends Fragment {

    public FragmentEventPrecessMain() {
    }


    public static FragmentEventPrecessMain newInstance() { return new FragmentEventPrecessMain();}
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_process_main,container,false);


        return view;
    }
}
