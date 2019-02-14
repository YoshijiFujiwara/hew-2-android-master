package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.SelectedSession;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.BudgetActualListAdapter;
import com.hew.second.gathering.views.adapters.BudgetEstimateListAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BudgetEstimateFragment extends BudgetFragment {

    EditText budget_estimate_tv;
    ListView budget_estimate_lv;

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

        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_budget_estimate, container, false);

        SystemClock.sleep(50); //todo ちょっとずらしてやればできるけど、最悪なコード
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
                            Log.d("api", "apiestimate：" + list.data.name.toString());

                            ArrayList<String> nameArray = new ArrayList<>();
                            ArrayList<String> infoArray = new ArrayList<>();
                            // session情報から,usernameのリストを生成
                            for (int i = 0; i < list.data.users.size(); i++) {
                                nameArray.add(list.data.users.get(i).username);
                                infoArray.add("情報"); // todo あとで計算する
                            }
                            String[] nameParams = nameArray.toArray(new String[nameArray.size()]);
                            String[] infoParams = infoArray.toArray(new String[infoArray.size()]);
                            BudgetEstimateListAdapter budgetEstimateListAdapter = new BudgetEstimateListAdapter(activity, nameParams, infoParams);
                            budget_estimate_lv = (ListView) view.findViewById(R.id.budget_estimate_list);
                            budget_estimate_lv.setAdapter(budgetEstimateListAdapter);
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

    private void updateView(SessionDetail data) {

    }
}
