package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.SelectedSession;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.BudgetActualListAdapter;
import com.hew.second.gathering.views.adapters.BudgetEstimateListAdapter;
import com.hew.second.gathering.views.adapters.BudgetFragmantPagerAdapter;
import com.hew.second.gathering.R;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BudgetFragment extends Fragment {

    int estimateBudget, actualBudget; // 予算額と実際にかかった金額
    Session session; // session情報
    String name;

    public static BudgetFragment newInstance() {
        return new BudgetFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // todo sessionidを1にセット
        SelectedSession.setSessionId(getActivity().getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE), 1);
        Log.v("sessionid", "" + SelectedSession.getSharedSessionId(getActivity().getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)));

        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_budget, container, false);
//        View estimateView = inflater.inflate(R.layout.fragment_budget_estimate, container, false);
//        View actualView = inflater.inflate(R.layout.fragment_budget_actual, container, false);
//
//        // sharedPreferenceに格納されているsessionIdから、session情報を取得する
//        ApiService service = Util.getService();
//        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
//        token.subscribeOn(Schedulers.io())
//                .flatMap(result -> {
//                    LoginUser.setToken(result.access_token);
//                    return service.getSessionDetail(LoginUser.getToken(), SelectedSession.getSharedSessionId(activity.getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)));
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .unsubscribeOn(Schedulers.io())
//                .subscribe(
//                        list -> {
//                            // session情報をセットする
//                            Log.d("api", "apiestimate：" + list.data.name.toString());
//
//                            // estimateフラグメントのリストビューの作成
//                            ArrayList<String> nameArray = new ArrayList<>();
//                            ArrayList<String> infoArray = new ArrayList<>();
//                            // session情報から,usernameのリストを生成
//                            for (int i = 0; i < list.data.users.size(); i++) {
//                                nameArray.add(list.data.users.get(i).username);
//                                infoArray.add("情報"); // todo あとで計算する
//                            }
//                            String[] nameParams = nameArray.toArray(new String[nameArray.size()]);
//                            String[] infoParams = infoArray.toArray(new String[infoArray.size()]);
//                            BudgetEstimateListAdapter budgetEstimateListAdapter = new BudgetEstimateListAdapter(activity, nameParams, infoParams);
//                            ListView budget_estimate_lv = (ListView) estimateView.findViewById(R.id.budget_estimate_list);
//                            budget_estimate_lv.setAdapter(budgetEstimateListAdapter);
//
////
////                            // actualフラグメントのリストビューの生成
////                            ArrayList<String> actualNameArray = new ArrayList<>();
////                            ArrayList<Integer> costNameArray = new ArrayList<>();
////                            // session情報から,usernameのリストを生成
////                            for (int i = 0; i < list.data.users.size(); i++) {
////                                actualNameArray.add(list.data.users.get(i).username);
////                                costNameArray.add(1000); // todo あとで計算する
////                            }
////                            String[] actualNameParams = actualNameArray.toArray(new String[actualNameArray.size()]);
////                            Integer[] actualCostParams = costNameArray.toArray(new Integer[costNameArray.size()]);
////                            BudgetActualListAdapter budgetActualListAdapter = new BudgetActualListAdapter(activity, actualNameParams, actualCostParams);
////                            ListView budget_actual_lv = (ListView) actualView.findViewById(R.id.budget_actual_list);
////                            budget_actual_lv.setAdapter(budgetActualListAdapter);
//
//
//                        },  // 成功時
//                        throwable -> {
//                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
//                        }
//                );
//


        BudgetFragmantPagerAdapter adapter = new BudgetFragmantPagerAdapter(getFragmentManager());
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
    }

    private void setSessionInfo(Session data) {
        this.session = data;
        this.name = data.name;
    }
}
