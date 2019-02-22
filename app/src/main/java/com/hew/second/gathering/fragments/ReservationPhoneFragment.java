package com.hew.second.gathering.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hew.second.gathering.R;
// お店に予約の電話
public class ReservationPhoneFragment extends SessionBaseFragment {



    public static  ReservationPhoneFragment newInstance() {
        Bundle args = new Bundle();
        ReservationPhoneFragment fragment = new ReservationPhoneFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_reservation_phone,container,false);
        return view;
    }
}
