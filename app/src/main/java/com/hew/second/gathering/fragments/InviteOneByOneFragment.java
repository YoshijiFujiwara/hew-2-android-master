package com.hew.second.gathering.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hew.second.gathering.R;

public class InviteOneByOneFragment extends Fragment {

    public static InviteOneByOneFragment newInstance() {
        return new InviteOneByOneFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_one_by_one, container, false);
        return view;
    }
}
