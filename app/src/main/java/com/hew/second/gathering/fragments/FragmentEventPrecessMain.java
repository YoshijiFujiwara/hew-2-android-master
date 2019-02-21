package com.hew.second.gathering.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

        if (getFragmentManager() != null) {

//          FragmentManagerからFragmentTransactionを作成
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

//           FragmentにActivityに登録
            fragmentTransaction.replace(R.id.sip_container,FragmentEventFinish.newInstance());
//          バックスタックに登録
            fragmentTransaction.addToBackStack(null);
//           上記の変更を登録
            fragmentTransaction.commit();

        }



        return view;
    }
}
