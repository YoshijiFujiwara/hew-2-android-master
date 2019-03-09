package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.activities.AddDefaultSettingActivity;
import com.hew.second.gathering.activities.EditDefaultSettingActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.activities.MainActivity;
import com.hew.second.gathering.api.DefaultSetting;
import com.hew.second.gathering.api.DefaultSettingDetail;
import com.hew.second.gathering.api.DefaultSettingList;
import com.hew.second.gathering.api.GroupDetail;
import com.hew.second.gathering.api.GroupList;
import com.hew.second.gathering.views.adapters.DefaultSettingAdapter;
import com.hew.second.gathering.views.adapters.GroupAdapter;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.EditGroupActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Group;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static com.hew.second.gathering.activities.BaseActivity.INTENT_EDIT_DEFAULT;


public class DefaultSettingFragment extends BaseFragment {
    ArrayList<DefaultSettingAdapter.Data> ar = new ArrayList<DefaultSettingAdapter.Data>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    DefaultSettingAdapter adapter = null;

    public static DefaultSettingFragment newInstance() {
        return new DefaultSettingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_default,
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
        activity.setTitle("デフォルト設定");

        FloatingActionButton fab = activity.findViewById(R.id.fab_newDefault);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDefault();
            }
        });

        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        GridView gridView = activity.findViewById(R.id.gridView_default);

        SearchView searchView = activity.findViewById(R.id.searchView);
        searchView.setOnClickListener((v) -> {
            searchView.setIconified(false);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // 送信
                // focusout
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // テキスト変更
                return false;
            }
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            switch (view.getId()) {
                case R.id.delete_default:
                    new MaterialDialog.Builder(activity)
                            .title("デフォルト設定削除")
                            .content(adapter.getList().get(position).name+ "を削除しますか？")
                            .positiveText("OK")
                            .onPositive((dialog, which) -> {
                                deleteDefault(adapter.getList().get(position).id);
                            })
                            .negativeText("キャンセル")
                            .show();
                    break;
                default:
                    // 編集画面にデフォルトIDを渡す
                    Intent intent = new Intent(activity.getApplication(), EditDefaultSettingActivity.class);
                    intent.putExtra("DEFAULTSETTING_ID", ar.get(position).id);
                    startActivityForResult(intent, INTENT_EDIT_DEFAULT);
            }});
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchList();
    }

    private void createDefault() {
        Intent intent = new Intent(activity.getApplication(), AddDefaultSettingActivity.class);
        startActivityForResult(intent, INTENT_EDIT_DEFAULT);
    }

    private void fetchList() {
        mSwipeRefreshLayout.setRefreshing(true);
        ApiService service = Util.getService();
        Observable<DefaultSettingList> token = service.getDefaultSettingList();
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (activity != null) {
                                mSwipeRefreshLayout.setRefreshing(false);
                                updateList(list.data);
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            mSwipeRefreshLayout.setRefreshing(false);
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
        for (DefaultSetting m : data) {
            ar.add(new DefaultSettingAdapter.Data(m.id, m.name));
        }
        adapter = new DefaultSettingAdapter(ar);
        if(gridView != null) {
            // ListViewにadapterをセット
            gridView.setAdapter(adapter);
        }
    }

    private void deleteDefault(int id){
        ApiService service = Util.getService();
        Completable token = service.deleteDefaultSetting(id);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            if (activity != null) {
                                fetchList();
                                final Snackbar snackbar = Snackbar.make(getView(), "デフォルトを削除しました", Snackbar.LENGTH_LONG);
                                snackbar.getView().setBackgroundColor(Color.BLACK);
                                snackbar.setActionTextColor(Color.WHITE);
                                snackbar.show();
                            }
                        }, // 終了時
                        (throwable) -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                                if (activity != null && !cd.isDisposed() && throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                ));

    }

}

