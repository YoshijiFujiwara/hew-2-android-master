package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.AddDefaultSettingActivity;
import com.hew.second.gathering.activities.EditDefaultSettingActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.DefaultSetting;
import com.hew.second.gathering.api.DefaultSettingList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.ApplyDefaultSettingAdapter;
import com.hew.second.gathering.views.adapters.DefaultSettingAdapter;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static com.hew.second.gathering.activities.BaseActivity.INTENT_EDIT_DEFAULT;


public class ApplyDefaultFragment extends SessionBaseFragment {
    ArrayList<DefaultSetting> ar = new ArrayList<>();
    ApplyDefaultSettingAdapter adapter = null;

    public static ApplyDefaultFragment newInstance() {
        return new ApplyDefaultFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_apply_default,
                container, false);
        return view;
    }

    public void removeFocus() {
        SearchView searchView = activity.findViewById(R.id.searchView);
        searchView.clearFocus();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.setTitle("デフォルト設定選択");

        GridView gridView = activity.findViewById(R.id.gridView_default);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            // TODO:デフォルト設定を適用して店選択へ
            if(activity != null){
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                if(fragmentManager != null){
                    activity.defaultSetting = ar.get(position);
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.replace(R.id.eip_container, EditShopFragment.newInstance());
                    fragmentTransaction.commit();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchList();
    }

    private void fetchList() {
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        ApiService service = Util.getService();
        Observable<DefaultSettingList> token = service.getDefaultSettingList();
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (activity != null) {
                                updateList(list.data);
                                if(dialog != null){
                                    dialog.dismiss();
                                }
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if(dialog != null){
                                dialog.dismiss();
                            }

                            if (activity != null && !cd.isDisposed() && throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                ));
    }

    private void updateList(List<DefaultSetting> data) {
        // ListView生成
        GridView gridView = activity.findViewById(R.id.gridView_default);
        ar.clear();
        ar.addAll(data);
        ar.add(null);
        adapter = new ApplyDefaultSettingAdapter(ar);
        if (gridView != null) {
            // ListViewにadapterをセット
            gridView.setAdapter(adapter);
        }
    }

}

