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
import com.hew.second.gathering.views.adapters.BudgetActualListAdapter;
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

        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity != null) {
            View view = inflater.inflate(R.layout.fragment_budget_estimate, container, false);

            Session session = SelectedSession.getSessionDetail(fragmentActivity.getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE));
//            Log.v("sessionname", session.name);

            // 予算額があれば、セットする
            budget_estimate_tv = (EditText) view.findViewById(R.id.budget_estimate_tv);
            if (session.budget != 0) {
                budget_estimate_tv.setText(Integer.toString(session.budget), TextView.BufferType.EDITABLE);
            }

            // 予算額から、支払い予定額を計算する
            ArrayList<String> nameArray = new ArrayList<>();
            ArrayList<Integer> infoArray = new ArrayList<>();

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
                infoArray.add(managerCost);
                for (int i = 0; i < session.users.size(); i++) {
                    nameArray.add(session.users.get(i).username);
                    infoArray.add(managerCost + session.users.get(i).plus_minus);
                }

            } else {
                // 幹事情報をまずセットする
                nameArray.add(session.manager.username + "(幹事)");
                infoArray.add(0);
                // session情報から,usernameのリストを生成
                for (int i = 0; i < session.users.size(); i++) {
                    nameArray.add(session.users.get(i).username);
                    infoArray.add(0);
                }
            }

            String[] nameParams = nameArray.toArray(new String[nameArray.size()]);
            Integer[] infoParams = infoArray.toArray(new Integer[infoArray.size()]);
            BudgetEstimateListAdapter budgetEstimateListAdapter = new BudgetEstimateListAdapter(fragmentActivity, nameParams, infoParams);
            budget_estimate_lv = (ListView) view.findViewById(R.id.budget_estimate_list);
            budget_estimate_lv.setAdapter(budgetEstimateListAdapter);

            budget_update_btn = view.findViewById(R.id.budget_update_btn);
            budget_update_btn.setOnClickListener((v) -> {
                updateBudget(fragmentActivity, session, String.valueOf(budget_estimate_tv.getText()));

                // 再計算（汚い）
                ArrayList<String> nameArray2 = new ArrayList<>();
                ArrayList<Integer> infoArray2 = new ArrayList<>();

                int sum = Integer.parseInt(String.valueOf(budget_estimate_tv.getText()));
                // 幹事の金額は、支払い総額＋それぞれのplus_minusの和を、幹事を含めた人数で割ることで求められる
                int managerCost = 0;
                for (int i = 0; i < session.users.size(); i++) {
                    sum += session.users.get(i).plus_minus;
                }
                managerCost = sum / (session.users.size() + 1);

                // 幹事情報をまずセットする
                nameArray2.add(session.manager.username + "(幹事)");
                infoArray2.add(managerCost);
                for (int i = 0; i < session.users.size(); i++) {
                    nameArray2.add(session.users.get(i).username);
                    infoArray2.add(managerCost + session.users.get(i).plus_minus);
                }

                String[] nameParams2 = nameArray2.toArray(new String[nameArray2.size()]);
                Integer[] infoParams2 = infoArray2.toArray(new Integer[infoArray2.size()]);
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
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        token.subscribeOn(Schedulers.io())
            .flatMap(result -> {
                LoginUser.setToken(result.access_token);
                HashMap<String, String> body = new HashMap<>();
                body.put("name", session.name);
                body.put("shop_id", session.shop_id);
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
                        Toast.makeText(fragmentActivity, "予算を更新しました", Toast.LENGTH_LONG).show();

                    },  // 成功時
                    throwable -> {
                        Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                        Toast.makeText(fragmentActivity, "予算の更新に失敗しました", Toast.LENGTH_LONG).show();
                    }
            );
    }

//    private void addAttributeName(FragmentActivity fragmentActivity, int friend_id) {
//        ApiService service = Util.getService();
//        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
//        token.subscribeOn(Schedulers.io())
//            .flatMap(result -> {
//                LoginUser.setToken(result.access_token);
//                // sharedPreferenceからセッションIDを取得する
//                return service.getFriendDetail(LoginUser.getToken(), friend_id);
//            })
//            .observeOn(AndroidSchedulers.mainThread())
//            .unsubscribeOn(Schedulers.io())
//            .subscribe(
//                    list -> {
//                        Util.setLoading(false, fragmentActivity);
//                        Log.v("friend_id", String.valueOf(list.data.attribute));
//                        SelectedSession.infoArray.add(list.data.attribute.name);
//                    },  // 成功時
//                    throwable -> {
//                        Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
//                    }
//            );
//    }
}
