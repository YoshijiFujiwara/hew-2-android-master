package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.SelectedSession;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionUser;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.BudgetActualListAdapter;
import com.hew.second.gathering.views.adapters.BudgetEstimateListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BudgetActualFragment extends BudgetFragment {

    EditText budget_actual_tv;
    ListView budget_actual_lv;
    Button budget_actual_update_btn;

    public static BudgetActualFragment newInstance() {
        return new BudgetActualFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity != null) {
            View view = inflater.inflate(R.layout.fragment_budget_actual, container, false);
            Session session = SelectedSession.getSessionDetail(fragmentActivity.getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE));
            Log.v("sessoinActualNAME", session.name);

            // 実額があれば、セットする
            budget_actual_tv = (EditText) view.findViewById(R.id.budget_actual_tv);
            if (session.budget != 0) {
                budget_actual_tv.setText(Integer.toString(session.actual), TextView.BufferType.EDITABLE);
            }

            // 実額から、支払い金額を計算する

            ArrayList<String> nameArray = new ArrayList<>();
            ArrayList<Integer> costArray = new ArrayList<>();
            // 幹事情報をまずセットする
            nameArray.add(session.manager.username);
            costArray.add(0);
            // session情報から,usernameのリストを生成
            for (int i = 0; i < session.users.size(); i++) {
                nameArray.add(session.users.get(i).username);
                costArray.add(session.users.get(i).plus_minus);
            }
            String[] nameParams = nameArray.toArray(new String[nameArray.size()]);
            Integer[] costParams = costArray.toArray(new Integer[costArray.size()]);
            BudgetActualListAdapter budgetActualListAdapter = new BudgetActualListAdapter(fragmentActivity, nameParams, costParams);
            budget_actual_lv = (ListView) view.findViewById(R.id.budget_actual_list);
            budget_actual_lv.setAdapter(budgetActualListAdapter);

            budget_actual_update_btn = view.findViewById(R.id.budget_actual_update_btn);
            budget_actual_update_btn.setOnClickListener((v) -> {
                updateBudgetActual(fragmentActivity, session, String.valueOf(budget_actual_tv.getText()));
            });

            return view;
        }
        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity != null) {
            Util.setLoading(true, getActivity());
        }
    }

    private void updateBudgetActual(FragmentActivity fragmentActivity, Session session, String budgetText) {
        ApiService service = Util.getService();
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    HashMap<String, String> body = new HashMap<>();
                    body.put("name", session.name);
                    body.put("shop_id", session.shop_id);
                    body.put("budget", Integer.toString(session.budget));
                    body.put("actual", budgetText);
                    body.put("start_time", session.start_time);
                    body.put("end_time", session.end_time);
                    // sharedPreferenceからセッションIDを取得する
                    return service.updateSession(LoginUser.getToken(),
                            SelectedSession.getSharedSessionId(fragmentActivity.getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)),
                            body
                    );
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Util.setLoading(false, fragmentActivity);
                            Log.v("sessioninfo", list.data.name);

                            // sharedPreferenceにsessionの詳細情報を渡す
                            SelectedSession.setSessionDetail(fragmentActivity.getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE), list.data);
                            Toast.makeText(fragmentActivity, "実額を更新しました", Toast.LENGTH_LONG).show();

                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            Toast.makeText(fragmentActivity, "実額の更新に失敗しました", Toast.LENGTH_LONG).show();
                        }
                );
    }
}
