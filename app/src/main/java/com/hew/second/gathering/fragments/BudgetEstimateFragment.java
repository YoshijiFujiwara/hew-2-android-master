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

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.BudgetEstimateListAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class BudgetEstimateFragment extends SessionBaseFragment {

    EditText budget_estimate_tv;
    ListView budget_estimate_lv;
    Button budget_update_btn;
    String attributeName;

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

        FragmentActivity fragmentActivity = activity;
        if (fragmentActivity != null) {
            View view = inflater.inflate(R.layout.fragment_budget_estimate, container, false);

            Session session = activity.session;

            // 予算額があれば、セットする
            budget_estimate_tv = (EditText) view.findViewById(R.id.budget_estimate_tv);
            if (session.budget != 0) {
                budget_estimate_tv.setText(Integer.toString(session.budget), TextView.BufferType.EDITABLE);
            }

            // 予算額から、支払い予定額を計算する
            ArrayList<String> nameArray = new ArrayList<>();
            ArrayList<Integer> costArray = new ArrayList<>();

            // 実額から、支払い金額を計算する
            if (session.budget != 0) {
                int sum = session.budget;
                Log.v("予算額", String.valueOf(sum));
                // 幹事の金額は、支払い総額＋それぞれのplus_minusの和を、幹事を含めた人数で割ることで求められる
                int managerCost = 0;
                for (int i = 0; i < session.users.size(); i++) {
                    sum += session.users.get(i).plus_minus;
                }
                managerCost = sum / (session.users.size() + 1);

                // 幹事情報をまずセットする
                nameArray.add(session.manager.username + "(幹事)");
                costArray.add(managerCost);
                for (int i = 0; i < session.users.size(); i++) {
                    nameArray.add(session.users.get(i).username);
                    costArray.add(managerCost + session.users.get(i).plus_minus);
                }

            } else {
                // 幹事情報をまずセットする
                nameArray.add(session.manager.username + "(幹事)");
                costArray.add(0);
                // session情報から,usernameのリストを生成
                for (int i = 0; i < session.users.size(); i++) {
                    nameArray.add(session.users.get(i).username);
                    costArray.add(0);
                }
            }

            String[] nameParams = nameArray.toArray(new String[nameArray.size()]);
            Integer[] infoParams = costArray.toArray(new Integer[costArray.size()]);
            BudgetEstimateListAdapter budgetEstimateListAdapter = new BudgetEstimateListAdapter(fragmentActivity, nameParams, infoParams);
            budget_estimate_lv = (ListView) view.findViewById(R.id.budget_estimate_list);
            budget_estimate_lv.setAdapter(budgetEstimateListAdapter);

            budget_update_btn = view.findViewById(R.id.budget_update_btn);
            budget_update_btn.setOnClickListener((v) -> {
                updateBudget(fragmentActivity, session, String.valueOf(budget_estimate_tv.getText()));

                // 再計算（汚い）
                ArrayList<String> nameArray2 = new ArrayList<>();
                ArrayList<Integer> costArray2 = new ArrayList<>();

                int sum = Integer.parseInt(String.valueOf(budget_estimate_tv.getText()));
                // 幹事の金額は、支払い総額＋それぞれのplus_minusの和を、幹事を含めた人数で割ることで求められる
                int managerCost = 0;
                for (int i = 0; i < session.users.size(); i++) {
                    sum += session.users.get(i).plus_minus;
                }
                managerCost = sum / (session.users.size() + 1);

                // 幹事情報をまずセットする
                nameArray2.add(session.manager.username + "(幹事)");
                costArray2.add(managerCost);
                for (int i = 0; i < session.users.size(); i++) {
                    nameArray2.add(session.users.get(i).username);
                    costArray2.add(managerCost + session.users.get(i).plus_minus);
                }

                String[] nameParams2 = nameArray2.toArray(new String[nameArray2.size()]);
                Integer[] infoParams2 = costArray2.toArray(new Integer[costArray2.size()]);
                BudgetEstimateListAdapter budgetEstimateListAdapter2 = new BudgetEstimateListAdapter(fragmentActivity, nameParams2, infoParams2);
                budget_estimate_lv.setAdapter(budgetEstimateListAdapter2);
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
        HashMap<String, String> body = new HashMap<>();
        body.put("name", session.name);
        body.put("shop_id", session.shop_id);
        body.put("budget", budgetText);
        body.put("actual", Integer.toString(session.actual));
        body.put("start_time", session.start_time);
        body.put("end_time", session.end_time);
        Observable<SessionDetail> token = service.updateSession(LoginUser.getToken(), activity.session.id, body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Log.v("sessioninfo", list.data.name);
                            if(activity != null){
                                activity.session = list.data;
                            }

                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    // ログインアクティビティへ遷移
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                ));
    }
}
