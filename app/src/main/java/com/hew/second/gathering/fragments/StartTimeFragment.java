package com.hew.second.gathering.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hew.second.gathering.R;

//　開始時刻設定
public class StartTimeFragment extends SessionBaseFragment {

    public static StartTimeFragment newInstance() {
        Bundle args = new Bundle();
        StartTimeFragment fragment = new StartTimeFragment();
        fragment.setArguments(args);
        return fragment;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//
        TextView startDateText = getActivity().findViewById(R.id.start_date);
        TextView startTimeText = getActivity().findViewById(R.id.start_timer);
        TextView endDateText = getActivity().findViewById(R.id.end_date);
        TextView endTimeText = getActivity().findViewById(R.id.end_timer);


        view = inflater.inflate(R.layout.fragment_starttime,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        if (savedInstanceState == null) {

            DatePickerDialogFragment datePicker = new DatePickerDialogFragment();
        datePicker.show(getFragmentManager(), "datePicker");

        }
    }
}
