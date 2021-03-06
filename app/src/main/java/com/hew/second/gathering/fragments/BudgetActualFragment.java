package com.hew.second.gathering.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.EventProcessMainActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.SessionUserDetail;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.BudgetActualListAdapter;

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

public class BudgetActualFragment extends SessionBaseFragment {

    EditText budget_actual_et;
    ListView budget_actual_lv;
    Button budget_actual_update_btn;
    Spinner budget_actual_spinner;


    public static BudgetActualFragment newInstance() {
        return new BudgetActualFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (activity != null) {
            view = inflater.inflate(R.layout.fragment_budget_actual, container, false);
            budget_actual_spinner = view.findViewById(R.id.budget_actual_spinner);
            String unitRounding = budget_actual_spinner.getSelectedItem().toString();

            // todo ハードコーディング中(activity.session.unit_rounding_budgetから取ってくる)
            String unitRoundingActual = activity.session.unit_rounding_actual;
            Log.v("unit_rounding", activity.session.unit_rounding_actual);
            if (unitRoundingActual.equals("1")) {
                budget_actual_spinner.setSelection(0);
            } else if (unitRoundingActual.equals("10")) {
                budget_actual_spinner.setSelection(1);
            } else if (unitRoundingActual.equals("100")) {
                budget_actual_spinner.setSelection(2);
            } else if (unitRoundingActual.equals("1000")) {
                budget_actual_spinner.setSelection(3);
            } else if (unitRoundingActual.equals("10000")) {
                budget_actual_spinner.setSelection(4);
            }

            Log.v("sessoinActualNAME", activity.session.name);

            // 実額があれば、セットする
            budget_actual_et = (EditText) view.findViewById(R.id.budget_actual_et);
            if (activity.session.actual != 0) {
                budget_actual_et.setText(String.format("%,d", activity.session.actual), TextView.BufferType.EDITABLE);
            }

            budget_actual_et.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        return true;
                    }
                    return false;
                }
            });

            // 3桁区切り
            budget_actual_et.addTextChangedListener(new TextWatcher() {
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
                        budget_actual_et.setText("");
                    }
                    isEdiging = false;
                }
            });

            updateListView(activity.session, Integer.parseInt(unitRounding));


            budget_actual_update_btn = view.findViewById(R.id.budget_actual_update_btn);
            budget_actual_update_btn.setOnClickListener((v) -> {
                budget_actual_update_btn.setEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        budget_actual_update_btn.setEnabled(true);
                    }
                }, 1000);
                removeFocus();
                if(budget_actual_et.getText().toString().isEmpty()){
                    final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "金額を入力してください。", Snackbar.LENGTH_SHORT);
                    snackbar.getView().setBackgroundColor(Color.BLACK);
                    snackbar.setActionTextColor(Color.WHITE);
                    snackbar.show();
                }else{
                    updateBudgetActual(activity, view, activity.session, String.valueOf(budget_actual_et.getText()).replace(",", ""));
                }
            });

            budget_actual_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Log.v("updatedUnitRounding", String.valueOf(position));
                    int updatedUnitRoundingActual = 1;
                    if (position == 0) {
                        updatedUnitRoundingActual = 1;
                    } else if (position == 1) {
                        updatedUnitRoundingActual = 10;
                    } else if (position == 2) {
                        updatedUnitRoundingActual = 100;
                    } else if (position == 3) {
                        updatedUnitRoundingActual = 1000;
                    } else if (position == 4) {
                        updatedUnitRoundingActual = 10000;
                    }
                    updateUnitRoundingActual(activity.session, updatedUnitRoundingActual);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // activity.session　の情報を更新する
    public void updateSessionInfo(Session session, int unitRounding) {
        ApiService service = Util.getService();
        Observable<SessionDetail> token = service.getSessionDetail(session.id);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Log.v("sessioninfo", list.data.name);
                            if(activity != null){
                                updateListView(list.data, unitRounding);
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
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
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                        }
                ));
    }

    private void updateBudgetActual(FragmentActivity fragmentActivity, View view, Session session, String budgetActualText) {
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("name", session.name);
        body.put("shop_id", session.shop_id);
        body.put("budget", Integer.toString(session.budget));
        body.put("actual", budgetActualText);
        body.put("start_time", session.start_time);
        body.put("end_time", session.end_time);
        Observable<SessionDetail> token = service.updateSession(session.id, body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Log.v("sessioninfo", list.data.name);
                            if(activity != null){
                                updateSessionInfo(activity.session,  Integer.parseInt(budget_actual_spinner.getSelectedItem().toString()));
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
                                dialog.dismiss();
                            }
                        }
                ));
    }

    private void updateListView(Session session, int unitRounding) {
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
            int allowUserCount = 0;
            for (int i = 0; i < session.users.size(); i++) {
                if (new String("allow").equals(session.users.get(i).join_status)) {
                    allowUserCount++;
                    sum -= session.users.get(i).plus_minus;
                }
            }
            Log.v("allow user count", String.valueOf(allowUserCount));
            managerCost = sum / (allowUserCount + 1);
            managerCost = ((sum / unitRounding) / (allowUserCount + 1)) * unitRounding;

            // 幹事情報をまずセットする
            nameArray.add(session.manager.username);
            costArray.add(managerCost);
            paidArray.add(false);
            userIdArray.add(String.valueOf(session.manager.id));
            for (int i = 0; i < session.users.size(); i++) {
                if (new String("allow").equals(session.users.get(i).join_status)) {
                    nameArray.add(session.users.get(i).username);
                    costArray.add(managerCost + session.users.get(i).plus_minus);
                    userIdArray.add(String.valueOf(session.users.get(i).id));
                    if (session.users.get(i).paid == 1) {
                        paidArray.add(true);
                    } else {
                        paidArray.add(false);
                    }
                }
            }

            // 差分を幹事に上乗せする処理
            int $costArraySum = 0;
            for (int i = 0; i < costArray.size(); i++) {
                $costArraySum += costArray.get(i);
            }
            costArray.set(0, costArray.get(0) + (session.actual - $costArraySum));

        } else {
            // 幹事情報をまずセットする
            nameArray.add(session.manager.username);
            costArray.add(0);
            paidArray.add(false);
            userIdArray.add(String.valueOf(session.manager.id));
            // session情報から,usernameのリストを生成
            for (int i = 0; i < session.users.size(); i++) {
                if (new String("allow").equals(session.users.get(i).join_status)) {
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
        }

        String[] nameParams = nameArray.toArray(new String[nameArray.size()]);
        Integer[] costParams = costArray.toArray(new Integer[costArray.size()]);
        Boolean[] paidParams = paidArray.toArray(new Boolean[paidArray.size()]);
        String[] userIdParams = userIdArray.toArray(new String[userIdArray.size()]);
        BudgetActualListAdapter budgetActualListAdapter = new BudgetActualListAdapter(activity, nameParams, costParams, paidParams, userIdParams, session.id);
        budget_actual_lv = (ListView) view.findViewById(R.id.budget_actual_list);
        budget_actual_lv.setAdapter(budgetActualListAdapter);
    }

    private void updateUnitRoundingActual(Session session, int unitRounding) {
        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("unit_rounding_actual", String.valueOf(unitRounding));
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

    // このフラグメントが選ばれた時
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // 自動で実額に適用するように見せる
        if (activity != null) {
            if (activity.session.actual != 0) {
                budget_actual_et.setText(String.valueOf(activity.session.actual));
            }
        }
    }
    public void removeFocus() {
        InputMethodManager inputMethodMgr = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
        inputMethodMgr.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
