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
import android.widget.Toast;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.SessionUserDetail;
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

        if (activity != null) {
            view = inflater.inflate(R.layout.fragment_budget_actual, container, false);

            Log.v("sessoinActualNAME", activity.session.name);

            // 実額があれば、セットする
            budget_actual_tv = (EditText) view.findViewById(R.id.budget_actual_tv);
            if (activity.session.budget != 0) {
                budget_actual_tv.setText(Integer.toString(activity.session.actual), TextView.BufferType.EDITABLE);
            }

            updateListView();


            budget_actual_update_btn = view.findViewById(R.id.budget_actual_update_btn);
            budget_actual_update_btn.setOnClickListener((v) -> {
                updateBudgetActual(activity, view, activity.session, String.valueOf(budget_actual_tv.getText()));
                // リストビューを空にする
                budget_actual_lv.setAdapter(new BudgetActualListAdapter(activity, new String[0], new Integer[0], new Boolean[0], new String[0]));
                activity.session.actual = Integer.parseInt(String.valueOf(budget_actual_tv.getText()));
                // 再計算（汚い）
                updateListView();
            });


            // リストビューのクリックイベント
            budget_actual_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView list = (ListView) parent;
                    String clickedUserId = "ItemClick : " + (String)list.getItemAtPosition(position);

                    // クリックされたuseridの支払い状況を反転させる処理をして、画面を更新する
                    switchPaid(clickedUserId);
                    updateSessionInfo();
                    updateListView();
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
    public void updateSessionInfo() {
        ApiService service = Util.getService();
        Observable<SessionDetail> token = service.getSessionDetail(LoginUser.getToken(), activity.session.id);
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

    private void updateListView() {
        ArrayList<String> nameArray = new ArrayList<>();
        ArrayList<Integer> costArray = new ArrayList<>();
        ArrayList<String> userIdArray = new ArrayList<>();
        ArrayList<Boolean> paidArray = new ArrayList<>();

        // 実額から、支払い金額を計算する
        if (activity.session.actual != 0) {
            int sum = activity.session.actual;
            Log.v("総支払額", String.valueOf(sum));
            // 幹事の金額は、支払い総額＋それぞれのplus_minusの和を、幹事を含めた人数で割ることで求められる
            int managerCost = 0;
            for (int i = 0; i < activity.session.users.size(); i++) {
                sum += activity.session.users.get(i).plus_minus;
            }
            managerCost = sum / (activity.session.users.size() + 1);

            // 幹事情報をまずセットする
            nameArray.add(activity.session.manager.username + "(幹事)");
            costArray.add(managerCost);
            paidArray.add(false);
            userIdArray.add(String.valueOf(activity.session.manager.id));
            for (int i = 0; i < activity.session.users.size(); i++) {
                nameArray.add(activity.session.users.get(i).username);
                costArray.add(managerCost + activity.session.users.get(i).plus_minus);
                userIdArray.add(String.valueOf(activity.session.users.get(i).id));
                if (activity.session.users.get(i).paid == 1) {
                    paidArray.add(true);
                } else {
                    paidArray.add(false);
                }
            }

        } else {
            // 幹事情報をまずセットする
            nameArray.add(activity.session.manager.username + "(幹事)");
            costArray.add(0);
            userIdArray.add(String.valueOf(activity.session.manager.id));
            // session情報から,usernameのリストを生成
            for (int i = 0; i < activity.session.users.size(); i++) {
                nameArray.add(activity.session.users.get(i).username);
                costArray.add(0);
                userIdArray.add(String.valueOf(activity.session.users.get(i).id));
                if (activity.session.users.get(i).paid == 1) {
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
        BudgetActualListAdapter budgetActualListAdapter = new BudgetActualListAdapter(activity, nameParams, costParams, paidParams, userIdParams);
        budget_actual_lv = (ListView) view.findViewById(R.id.budget_actual_list);
        budget_actual_lv.setAdapter(budgetActualListAdapter);
    }

    /**
     * 指定したuserの支払い状況を反転する処理
     * @param userId
     */
    private void switchPaid(String userId) {
        Toast.makeText(activity, "test", Toast.LENGTH_SHORT).show();
        return ;
//        ApiService service = Util.getService();
//        Observable<SessionUserDetail> token = service.sessionUserSwitchPaid(LoginUser.getToken(), activity.session.id, Integer.parseInt(userId));
//        cd.add(token.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .unsubscribeOn(Schedulers.io())
//                .subscribe(
//                        list -> {
//                            if (activity != null) {
//                                Toast.makeText(activity, "test", Toast.LENGTH_SHORT).show();
//                            }
//                        },
//                        throwable -> {
//                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
//                            if (activity != null && !cd.isDisposed()) {
//                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
//                                    // ログインアクティビティへ遷移
//                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
//                                    startActivity(intent);
//                                }
//                            }
//                        }
//                ));
    }
}
