package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hew.second.gathering.activities.EventProcessMainActivity;

import io.reactivex.disposables.CompositeDisposable;

public abstract class SessionBaseFragment extends BaseFragment {

    protected EventProcessMainActivity activity = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EventProcessMainActivity) {
            activity = (EventProcessMainActivity) context;
        }
    }
}
