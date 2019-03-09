package com.hew.second.gathering.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class BudgetEstimateFragment extends SessionBaseFragment {

    EditText budget_estimate_et;
    ListView budget_estimate_lv;
    Button budget_update_btn;
    String attributeName;
    Spinner budget_estimate_spinner;

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
            view = inflater.inflate(R.layout.fragment_budget_estimate, container, false);
            budget_estimate_spinner = view.findViewById(R.id.budget_estimate_spinner);
            String unitRounding = budget_estimate_spinner.getSelectedItem().toString();
            Log.v("unitRounding", unitRounding);

            // todo ハードコーディング中(activity.session.unit_rounding_budgetから取ってくる)
            String unitRoundingEstimate = activity.session.unit_rounding_budget;
            //Log.v("unitRounding", unitRoundingEstimate);
            if (unitRoundingEstimate.equals("1")) {
                budget_estimate_spinner.setSelection(0);
            } else if (unitRoundingEstimate.equals("10")) {
                budget_estimate_spinner.setSelection(1);
            } else if (unitRoundingEstimate.equals("100")) {
                budget_estimate_spinner.setSelection(2);
            } else if (unitRoundingEstimate.equals("1000")) {
                budget_estimate_spinner.setSelection(3);
            } else if (unitRoundingEstimate.equals("10000")) {
                budget_estimate_spinner.setSelection(4);
            }

            // 一人あたりの予算額があれば、セットする
            budget_estimate_et = (EditText) view.findViewById(R.id.budget_estimate_et);
            if (activity.session.budget != 0) {
                budget_estimate_et.setText(String.format("%,d", activity.session.budget), TextView.BufferType.EDITABLE);
            }

            // 3桁区切り
            budget_estimate_et.addTextChangedListener(new TextWatcher() {
                boolean isEdiging;

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (isEdiging) return;
                    isEdiging = true;
                    String str = s.toString().replaceAll("[^\\d]", "");
                    double s1 = 0;
                    try {
                        s1 = Double.parseDouble(str);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    NumberFormat nf2 = NumberFormat.getInstance(Locale.JAPANESE);
                    ((DecimalFormat) nf2).applyPattern("###,###.###");
                    s.replace(0, s.length(), nf2.format(s1));

                    if (s.toString().equals("0")) {
                        budget_estimate_et.setText("");
                    }
                    isEdiging = false;
                }
            });

            // 人数のカウント
            int allowUserCount = 0;
            for (int i = 0; i < activity.session.users.size(); i++) {
                if (new String("allow").equals(activity.session.users.get(i).join_status)) {
                    allowUserCount++;
                }
            }
            TextView budget_estimate_allow_user_number = view.findViewById(R.id.budget_estimate_allow_user_number);
            budget_estimate_allow_user_number.setText(String.valueOf(allowUserCount + 1));

            updateListView(activity.session, Integer.parseInt(unitRounding));
            TextView budget_estimate_sum_tv = view.findViewById(R.id.budget_estimate_sum_tv);
            budget_estimate_sum_tv.setText(String.format("%,d", activity.session.budget * (allowUserCount + 1)) + "円");

            budget_update_btn = view.findViewById(R.id.budget_update_btn);
            budget_update_btn.setOnClickListener((v) -> {
                try{
                    activity.session.budget = Integer.parseInt(String.valueOf(budget_estimate_et.getText()).replace(",", ""));
                    // allowUserの処理
                    int allowUserCountLambda = 0;
                    for (int i = 0; i < activity.session.users.size(); i++) {
                        if (new String("allow").equals(activity.session.users.get(i).join_status)) {
                            allowUserCountLambda++;
                        }
                    }
                    updateBudget(fragmentActivity, activity.session, String.valueOf(budget_estimate_et.getText()).replace(",", ""), allowUserCountLambda);

                    budget_estimate_allow_user_number.setText(String.valueOf(allowUserCountLambda + 1));

                    int budgetSum = updateListView(activity.session, Integer.parseInt(unitRounding));
                    budget_estimate_sum_tv.setText(String.format("%,d", activity.session.budget * (allowUserCountLambda + 1)) + "円");

                }catch (Exception e){
                    final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "金額を入力してください。", Snackbar.LENGTH_SHORT);
                    snackbar.getView().setBackgroundColor(Color.BLACK);
                    snackbar.setActionTextColor(Color.WHITE);
                    snackbar.show();
                }

            });

            budget_estimate_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.v("updatedUnitRounding", String.valueOf(position));
                    int updatedUnitRoundingEstimate = 1;
                    if (position == 0) {
                        updatedUnitRoundingEstimate = 1;
                    } else if (position == 1) {
                        updatedUnitRoundingEstimate = 10;
                    } else if (position == 2) {
                        updatedUnitRoundingEstimate = 100;
                    } else if (position == 3) {
                        updatedUnitRoundingEstimate = 1000;
                    } else if (position == 4) {
                        updatedUnitRoundingEstimate = 10000;
                    }
                    updateUnitRoundingEstimate(activity.session, updatedUnitRoundingEstimate);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
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

    private void updateBudget(FragmentActivity fragmentActivity, Session session, String budgetText, int allowUserCount) {
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("budget", budgetText);
        // 実額が0の場合、総額を実額に自動適用する
        if (session.actual == 0) {
            body.put("actual", String.valueOf(Integer.parseInt(budgetText) * (allowUserCount + 1)));
        }

        Observable<SessionDetail> token = service.updateSession(session.id, body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Log.v("sessioninfo", list.data.name);
                            if (activity != null) {
                                activity.session = list.data;
                                dialog.dismiss();
                                updateSessionInfo(activity.session, Integer.parseInt(budget_estimate_spinner.getSelectedItem().toString()));
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
                                if (activity != null) {
                                    dialog.dismiss();
                                }
                            }
                        }
                ));
    }

    private int updateListView(Session session, int unitRounding) {
        // 予算額から、支払い予定額を計算する
        ArrayList<String> nameArray = new ArrayList<>();
        ArrayList<Integer> costArray = new ArrayList<>();
        ArrayList<Integer> plusMinusArray = new ArrayList<>();
        ArrayList<String> attributeArray = new ArrayList<>();
        ArrayList<String> userIdArray = new ArrayList<>();
        ArrayList<Boolean> allowedArray = new ArrayList<>(); // 幹事は自動でallowを適用する。その他は、join_statusを適用する

        int allowUserCount = 0;
        for (int i = 0; i < session.users.size(); i++) {
            if (new String("allow").equals(activity.session.users.get(i).join_status)) {
                allowUserCount++;
            }
        }

        // 実額から、支払い金額を計算する
        if (session.budget != 0) {
            // 幹事の金額は、支払い総額＋それぞれのplus_minusの和を、幹事を含めた人数で割ることで求められる
            int managerCost = 0;
            // sum は、一人あたりの予算額×allowUserの数
            int sum = session.budget * (allowUserCount + 1);
            for (int i = 0; i < session.users.size(); i++) {
                if (new String("allow").equals(activity.session.users.get(i).join_status)) {
                    sum -= session.users.get(i).plus_minus;
                }
            }
            Log.v("allow user count", String.valueOf(allowUserCount));
            managerCost = ((sum / unitRounding) / (allowUserCount + 1)) * unitRounding;

            // 幹事情報をまずセットする
            nameArray.add(session.manager.username);
            costArray.add(managerCost);
            plusMinusArray.add(0);
            attributeArray.add("幹事");
            userIdArray.add(String.valueOf(session.manager.id));
            allowedArray.add(true);
            for (int i = 0; i < session.users.size(); i++) {
                // denyの人はそもそも表示しない
                if (new String("allow").equals(activity.session.users.get(i).join_status)) {
                    nameArray.add(session.users.get(i).username);
                    costArray.add(managerCost + session.users.get(i).plus_minus);
                    plusMinusArray.add(session.users.get(i).plus_minus);
                    attributeArray.add(session.users.get(i).attribute_name);
                    userIdArray.add(String.valueOf(session.users.get(i).id));
                    allowedArray.add(true);
                }
            }

            // 差分を幹事に上乗せする処理
            int costArraySum = 0;
            for (int i = 0; i < costArray.size(); i++) {
                costArraySum += costArray.get(i);
            }
            costArray.set(0, costArray.get(0) + (session.budget * (allowUserCount + 1) - costArraySum));

            // 灰色表示の、　wait userを表示する
            for (int i = 0; i < session.users.size(); i++) {
                // denyの人はそもそも表示しない
                if (new String("wait").equals(activity.session.users.get(i).join_status)) {
                    nameArray.add(session.users.get(i).username);
                    costArray.add(managerCost + session.users.get(i).plus_minus);
                    plusMinusArray.add(session.users.get(i).plus_minus);
                    attributeArray.add(session.users.get(i).attribute_name);
                    userIdArray.add(String.valueOf(session.users.get(i).id));
                    allowedArray.add(false);
                }
            }

        } else {
            // 幹事情報をまずセットする
            nameArray.add(session.manager.username);
            costArray.add(0);
            plusMinusArray.add(0);
            attributeArray.add("幹事");
            userIdArray.add(String.valueOf(session.manager.id));
            allowedArray.add(true);
            // session情報から,usernameのリストを生成
            for (int i = 0; i < session.users.size(); i++) {
                if (new String("allow").equals(activity.session.users.get(i).join_status)) {
                    nameArray.add(session.users.get(i).username);
                    costArray.add(0);
                    plusMinusArray.add(session.users.get(i).plus_minus);
                    attributeArray.add(session.users.get(i).attribute_name);
                    userIdArray.add(String.valueOf(session.users.get(i).id));
                    // allow か　wait かで、背景色を分けて、わかりやすく表示するため
                    allowedArray.add(true);
                }
            }

            // session情報から,usernameのリストを生成
            for (int i = 0; i < session.users.size(); i++) {
                if (new String("wait").equals(activity.session.users.get(i).join_status)) {
                    nameArray.add(session.users.get(i).username);
                    costArray.add(0);
                    plusMinusArray.add(session.users.get(i).plus_minus);
                    attributeArray.add(session.users.get(i).attribute_name);
                    userIdArray.add(String.valueOf(session.users.get(i).id));
                    // allow か　wait かで、背景色を分けて、わかりやすく表示するため
                    allowedArray.add(false);
                }
            }
        }

        String[] nameParams = nameArray.toArray(new String[nameArray.size()]);
        Integer[] costParams = costArray.toArray(new Integer[costArray.size()]);
        Integer[] plusMinusParams = plusMinusArray.toArray(new Integer[plusMinusArray.size()]);
        String[] attributeNameParams = attributeArray.toArray(new String[attributeArray.size()]);
        String[] userIdParams = userIdArray.toArray(new String[userIdArray.size()]);
        Boolean[] allowedParams = allowedArray.toArray(new Boolean[allowedArray.size()]);
        BudgetEstimateListAdapter budgetEstimateListAdapter = new BudgetEstimateListAdapter(activity, nameParams, costParams, plusMinusParams, attributeNameParams, userIdParams, allowedParams, session.id);
        budget_estimate_lv = (ListView) view.findViewById(R.id.budget_estimate_list);
        budget_estimate_lv.setAdapter(budgetEstimateListAdapter);

        return session.budget * (allowUserCount + 1);
    }

    // activity.session　の情報を更新する
    public void updateSessionInfo(Session session, int unitRounding) {
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        ApiService service = Util.getService();
        Observable<SessionDetail> token = service.getSessionDetail(session.id);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Log.v("sessioninfo", list.data.name);
                            if (activity != null) {
                                updateListView(list.data, unitRounding);
                                dialog.dismiss();
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

    private void updateUnitRoundingEstimate(Session session, int unitRounding) {
        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("unit_rounding_budget", String.valueOf(unitRounding));
        Observable<SessionDetail> token = service.updateSession(session.id, body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Log.v("sessioninfo", list.data.name);
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
