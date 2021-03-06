package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.app.AlertDialog;
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

import dmax.dialog.SpotsDialog;
import icepick.Icepick;
import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseFragment extends Fragment {

    protected CompositeDisposable cd = new CompositeDisposable();
    protected Activity activity = null;
    protected View view = null;
    protected AlertDialog dialog = null;

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
        if(dialog != null){
            dialog.dismiss();
        }
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

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }
}
