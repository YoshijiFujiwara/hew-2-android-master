package com.hew.second.gathering.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hew.second.gathering.R;

public class WaitingPaymentFragment extends Fragment {
    public static WaitingPaymentFragment newInstance() {
        return new WaitingPaymentFragment();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waiting_payment,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//
//        ListView mlistView =(ListView) view.findViewById(R.id.waitingforpay_id);
//
//        // データを準備
//        ArrayList<String> items = new ArrayList<>();
//        for(int i = 0; i < 30; i++) { }
//
//        // Adapter - ArrayAdapter
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
//                this.getContext(),
//                R.layout.fragment_session_text,
//                items
//        );
//
//        // ListViewに表示;
//        mlistView.setAdapter(adapter);

    }
}
