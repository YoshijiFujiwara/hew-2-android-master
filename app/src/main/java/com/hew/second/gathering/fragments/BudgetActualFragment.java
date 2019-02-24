package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.hew.second.gathering.views.adapters.BudgetActualListAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class BudgetActualFragment extends SessionBaseFragment {

    EditText budget_actual_tv;
    ListView budget_actual_lv;
    Button budget_actual_update_btn;

    public static BudgetActualFragment newInstance() {
        return new BudgetActualFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentActivity fragmentActivity = activity;
        if (fragmentActivity != null) {
            view = inflater.inflate(R.layout.fragment_budget_actual, container, false);

            Session session = activity.session;
            Log.v("sessoinActualNAME", session.name);

            // 実額があれば、セットする
            budget_actual_tv = (EditText) view.findViewById(R.id.budget_actual_tv);
            if (session.budget != 0) {
                budget_actual_tv.setText(Integer.toString(session.actual), TextView.BufferType.EDITABLE);
            }

            ArrayList<String> nameArray = new ArrayList<>();
            ArrayList<Integer> costArray = new ArrayList<>();
            ArrayList<String> userIdArray = new ArrayList<>();
            ArrayList<Boolean> paidArray = new ArrayList<>();

            // 実額から、支払い金額を計算する
            if (session.actual != 0) {
                int sum = session.actual;
                Log.v("総支払額", String.valueOf(sum));
                // 幹事の金額は、支払い総額＋それぞれのplus_minusの和を、幹事を含めた人数で割ることで求められる
                int managerCost = 0;
                for (int i = 0; i < session.users.size(); i++) {
                    sum += session.users.get(i).plus_minus;
                }
                managerCost = sum / (session.users.size() + 1);

                // 幹事情報をまずセットする
                nameArray.add(session.manager.username + "(幹事)");
                costArray.add(managerCost);
                paidArray.add(false);
                userIdArray.add(String.valueOf(session.manager.id));
                for (int i = 0; i < session.users.size(); i++) {
                    nameArray.add(session.users.get(i).username);
                    costArray.add(managerCost + session.users.get(i).plus_minus);
                    userIdArray.add(String.valueOf(session.users.get(i).id));
                    if (session.users.get(i).paid == 1) {
                        paidArray.add(true);
                    } else {
                        paidArray.add(false);
                    }
                }

            } else {
                // 幹事情報をまずセットする
                nameArray.add(session.manager.username + "(幹事)");
                costArray.add(0);
                userIdArray.add(String.valueOf(session.manager.id));
                // session情報から,usernameのリストを生成
                for (int i = 0; i < session.users.size(); i++) {
                    nameArray.add(session.users.get(i).username);
                    costArray.add(0);
                    userIdArray.add(String.valueOf(session.users.get(i).id));
                    if (session.users.get(i).paid == 1) {
                        paidArray.add(true);
                    } else {
                        paidArray.add(false);
                    }
                }
            }

            String[] nameParams = nameArray.toArray(new String[nameArray.size()]);
            Integer[] costParams = costArray.toArray(new Integer[costArray.size()]);
            Boolean[] paidParams = paidArray.toArray(new Boolean[paidArray.size()]);
            String[] userIdParams = userIdArray.toArray(new String[userIdArray.size()]);
            BudgetActualListAdapter budgetActualListAdapter = new BudgetActualListAdapter(fragmentActivity, nameParams, costParams, paidParams, userIdParams);
            budget_actual_lv = (ListView) view.findViewById(R.id.budget_actual_list);
            budget_actual_lv.setAdapter(budgetActualListAdapter);

            budget_actual_update_btn = view.findViewById(R.id.budget_actual_update_btn);
            budget_actual_update_btn.setOnClickListener((v) -> {
                updateBudgetActual(fragmentActivity, view, session, String.valueOf(budget_actual_tv.getText()));
                // リストビューを空にする
                budget_actual_lv.setAdapter(new BudgetActualListAdapter(fragmentActivity, new String[0], new Integer[0], new Boolean[0], new String[0]));

                // 再計算（汚い）
                ArrayList<String> nameArray2 = new ArrayList<>();
                ArrayList<Integer> costArray2 = new ArrayList<>();
                ArrayList<String> userIdArray2 = new ArrayList<>();
                ArrayList<Boolean> paidArray2 = new ArrayList<>();

                int sum = Integer.parseInt(String.valueOf(budget_actual_tv.getText()));
                // 幹事の金額は、支払い総額＋それぞれのplus_minusの和を、幹事を含めた人数で割ることで求められる
                int managerCost = 0;
                for (int i = 0; i < session.users.size(); i++) {
                    sum += session.users.get(i).plus_minus;
                }
                managerCost = sum / (session.users.size() + 1);

                // 幹事情報をまずセットする
                nameArray2.add(session.manager.username + "(幹事)");
                costArray2.add(managerCost);
                userIdArray2.add(String.valueOf(session.manager.id));
                paidArray2.add(false);
                for (int i = 0; i < session.users.size(); i++) {
                    nameArray2.add(session.users.get(i).username);
                    costArray2.add(managerCost + session.users.get(i).plus_minus);
                    userIdArray2.add(String.valueOf(session.users.get(i).id));
                    if (session.users.get(i).paid == 1) {
                        paidArray2.add(true);
                    } else {
                        paidArray2.add(false);
                    }
                }

                String[] nameParams2 = nameArray2.toArray(new String[nameArray2.size()]);
                Integer[] costParams2 = costArray2.toArray(new Integer[costArray2.size()]);
                Boolean[] paidParam2 = paidArray2.toArray(new Boolean[paidArray2.size()]);
                String[] userIdParams2 = userIdArray2.toArray(new String[userIdArray2.size()]);

                BudgetActualListAdapter budgetActualListAdapter2 = new BudgetActualListAdapter(fragmentActivity, nameParams2, costParams2, paidParam2, userIdParams2);
                budget_actual_lv.setAdapter(budgetActualListAdapter2);
            });


            // リストビューのクリックイベント
            budget_actual_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView list = (ListView) parent;
                    String msg = "ItemClick : " + (String)list.getItemAtPosition(position);
                    Log.v("OnItemClick", msg);
                }
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
    }

    private void setSessionUserList(FragmentActivity fragmentActivity, View view) {

    }

    private void updateBudgetActual(FragmentActivity fragmentActivity, View view, Session session, String budgetActualText) {
        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("name", session.name);
        body.put("shop_id", session.shop_id);
        body.put("budget", Integer.toString(session.budget));
        body.put("actual", budgetActualText);
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
