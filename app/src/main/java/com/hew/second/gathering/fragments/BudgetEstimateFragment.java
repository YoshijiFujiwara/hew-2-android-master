package com.hew.second.gathering.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.SelectedSession;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.BudgetEstimateListAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BudgetEstimateFragment extends BudgetFragment {

    EditText budget_estimate_tv;
    ListView budget_estimate_lv;
    Button budget_update_btn;

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

            // 予算額があれば、セットする
            budget_estimate_tv = (EditText) view.findViewById(R.id.budget_estimate_tv);
            if (session.budget != 0) {
                budget_estimate_tv.setText(Integer.toString(session.budget), TextView.BufferType.EDITABLE);
            }

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

            budget_update_btn = view.findViewById(R.id.budget_update_btn);
            budget_update_btn.setOnClickListener((v) -> {

                updateBudget(fragmentActivity, session, String.valueOf(budget_estimate_tv.getText()));
            });
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

    private void updateBudget(FragmentActivity fragmentActivity, Session session, String budgetText) {
        ApiService service = Util.getService();
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    HashMap<String, String> body = new HashMap<>();
                    body.put("name", session.name);
                    body.put("shop_id", Integer.toString(session.shop_id));
                    body.put("budget", budgetText);
                    body.put("actual", Integer.toString(session.actual));
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
                            Toast.makeText(fragmentActivity, "予算を更新しました", Toast.LENGTH_LONG).show();

                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            Toast.makeText(fragmentActivity, "予算の更新に失敗しました", Toast.LENGTH_LONG).show();
                        }
                );
    }
}
