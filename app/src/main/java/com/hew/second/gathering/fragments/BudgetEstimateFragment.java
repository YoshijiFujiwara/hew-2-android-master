package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.SelectedSession;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.BudgetActualListAdapter;
import com.hew.second.gathering.views.adapters.BudgetEstimateListAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BudgetEstimateFragment extends BudgetFragment {

    EditText budget_estimate_tv;
    ListView budget_estimate_lv;

    public static BudgetEstimateFragment newInstance() {
        return new BudgetEstimateFragment();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity != null) {
            View view = inflater.inflate(R.layout.fragment_budget_estimate, container, false);
            Session session = SelectedSession.getSessionDetail(fragmentActivity.getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE));
            Log.v("sessionname", session.name);

            ArrayList<String> nameArray = new ArrayList<>();
            ArrayList<String> infoArray = new ArrayList<>();
            // session情報から,usernameのリストを生成
            for (int i = 0; i < session.users.size(); i++) {
                nameArray.add(session.users.get(i).username);
                infoArray.add("情報"); // todo あとで計算する
            }
            String[] nameParams = nameArray.toArray(new String[nameArray.size()]);
            String[] infoParams = infoArray.toArray(new String[infoArray.size()]);
            BudgetEstimateListAdapter budgetEstimateListAdapter = new BudgetEstimateListAdapter(fragmentActivity, nameParams, infoParams);
            budget_estimate_lv = (ListView) view.findViewById(R.id.budget_estimate_list);
            budget_estimate_lv.setAdapter(budgetEstimateListAdapter);

            return view;
        }
        return null;
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void updateView(SessionDetail data) {

    }
}
