package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hew.second.gathering.R;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseFragment extends Fragment {

    protected CompositeDisposable cd = new CompositeDisposable();
    protected Activity activity = null;
    protected View view = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            activity = (Activity) context;
        }
    }

    @Override
    public void onDetach() {
        cd.clear();
        activity = null;
        super.onDetach();
    }

    @Override
    public abstract View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState);

    @Override
    public void onDestroyView(){
        view = null;
        super.onDestroyView();
    }
}
