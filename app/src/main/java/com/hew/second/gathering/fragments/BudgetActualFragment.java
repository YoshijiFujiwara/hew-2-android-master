package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

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

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BudgetActualFragment extends BudgetFragment {

    EditText budget_actual_tv;
    ListView budget_actual_lv;

    public static BudgetActualFragment newInstance() {
        return new BudgetActualFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_budget_actual, container, false);

        // sharedPreferenceに格納されているsessionIdから、session情報を取得する
        ApiService service = Util.getService();
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    return service.getSessionDetail(LoginUser.getToken(), SelectedSession.getSharedSessionId(activity.getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            // session情報をセットする
                            Log.d("api", "api：" + list.data.name.toString());

                            ArrayList<String> nameArray = new ArrayList<>();
                            ArrayList<Integer> costArray = new ArrayList<>();
                            // session情報から,usernameのリストを生成
                            for (int i = 0; i < list.data.users.size(); i++) {
                                nameArray.add(list.data.users.get(i).username);
                                costArray.add(1000); // todo あとで計算する
                            }
                            String[] nameParams = nameArray.toArray(new String[nameArray.size()]);
                            Integer[] costParams = costArray.toArray(new Integer[costArray.size()]);
                            BudgetActualListAdapter budgetActualListAdapter = new BudgetActualListAdapter(activity, nameParams, costParams);
                            budget_actual_lv = (ListView) view.findViewById(R.id.budget_actual_list);
                            budget_actual_lv.setAdapter(budgetActualListAdapter);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                        }
                );


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

//        // sharedPreferenceに格納されているsessionIdから、session情報を取得する
//        ApiService service = Util.getService();
//        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
//        token.subscribeOn(Schedulers.io())
//                .flatMap(result -> {
//                    LoginUser.setToken(result.access_token);
//                    return service.getSessionDetail(LoginUser.getToken(), SelectedSession.getSharedSessionId(getActivity().getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)));
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .unsubscribeOn(Schedulers.io())
//                .subscribe(
//                        list -> {
//                            // session情報をセットする
//                            Log.d("api", "api：" + list.data.name.toString());
//
//                            ArrayList<String> nameArray = new ArrayList<>();
//                            ArrayList<Integer> costArray = new ArrayList<>();
//                            // session情報から,usernameのリストを生成
//                            for (int i = 0; i < list.data.users.size(); i++) {
//                                nameArray.add(list.data.users.get(i).username);
//                                costArray.add(1000); // todo あとで計算する
//                            }
//                            String[] nameParams = nameArray.toArray(new String[nameArray.size()]);
//                            Integer[] costParams = costArray.toArray(new Integer[costArray.size()]);
//                            BudgetActualListAdapter budgetActualListAdapter = new BudgetActualListAdapter(getActivity(), nameParams, costParams);
//                            budget_actual_lv = (ListView) getActivity().findViewById(R.id.budget_actual_list);
//                            budget_actual_lv.setAdapter(budgetActualListAdapter);
//                        },  // 成功時
//                        throwable -> {
//                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
//                        }
//                );
    }
}
