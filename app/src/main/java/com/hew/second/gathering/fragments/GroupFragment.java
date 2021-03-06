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
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.GroupDetail;
import com.hew.second.gathering.api.GroupList;
import com.hew.second.gathering.views.adapters.GroupAdapter;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.EditGroupActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Group;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static com.hew.second.gathering.activities.BaseActivity.INTENT_EDIT_GROUP;

public class GroupFragment extends BaseFragment {
    ArrayList<Group> ar = new ArrayList<>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    GroupAdapter adapter = null;

    public static GroupFragment newInstance() {
        return new GroupFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_group,
                container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.setTitle("グループ一覧");

        FloatingActionButton fab = activity.findViewById(R.id.fab_newGroup);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(activity)
                        .title("グループ名")
                        .content("タイトル")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .inputRangeRes(1, 30, R.color.colorAccentDark)
                        .input("グループ名", "", (MaterialDialog dialog, CharSequence input) -> {
                            createGroup(input.toString());
                        })
                        .negativeText("キャンセル")
                        .show();
            }
        });

        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        GridView gridView = activity.findViewById(R.id.gridView_group);
        gridView.setEmptyView(activity.findViewById(R.id.emptyView_group));

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            switch (view.getId()) {
                case R.id.delete_group:
                    new MaterialDialog.Builder(activity)
                            .title("グループ削除")
                            .content(adapter.getList().get(position).name+ "を削除しますか？")
                            .positiveText("OK")
                            .onPositive((dialog, which) -> {
                                deleteGroup(adapter.getList().get(position).id);
                            })
                            .negativeText("キャンセル")
                            .show();
                    break;
                default:
                    // メンバ編集画面へグループIDを渡す
                    Intent intent = new Intent(activity.getApplication(), EditGroupActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("GROUP_DETAIL", Parcels.wrap(ar.get(position)));
                    intent.putExtras(bundle);
                    startActivityForResult(intent, INTENT_EDIT_GROUP);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchList();
    }

    private void createGroup(String name) {
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("name", name);
        Observable<GroupDetail> token = service.createGroup(body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (activity != null) {
                                dialog.dismiss();
                                Intent intent = new Intent(activity.getApplication(), EditGroupActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("GROUP_DETAIL", Parcels.wrap(list.data));
                                bundle.putBoolean("NEW_GROUP", true);
                                intent.putExtras(bundle);
                                startActivityForResult(intent, INTENT_EDIT_GROUP);
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            dialog.dismiss();
                            if (activity != null && !cd.isDisposed() && throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                ));
    }

    private void fetchList() {
        mSwipeRefreshLayout.setRefreshing(true);
        ApiService service = Util.getService();
        Observable<GroupList> token = service.getGroupList();
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

    private void updateList(List<Group> data) {
        // ListView生成
        GridView gridView = activity.findViewById(R.id.gridView_group);
        ar.clear();
        ar.addAll(data);
        adapter = new GroupAdapter(ar);
        // ListViewにadapterをセット
        if(gridView != null)
        {
            gridView.setAdapter(adapter);
        }
    }

    private void deleteGroup(int id){
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        ApiService service = Util.getService();
        Completable token = service.deleteGroup(id);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            if (activity != null) {
                                fetchList();
                                dialog.dismiss();
                                final Snackbar snackbar = Snackbar.make(getView(), "グループを削除しました。", Snackbar.LENGTH_LONG);
                                snackbar.getView().setBackgroundColor(Color.BLACK);
                                snackbar.setActionTextColor(Color.WHITE);
                                snackbar.show();
                            }
                        }, // 終了時
                        (throwable) -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            dialog.dismiss();
                            if (activity != null && !cd.isDisposed()) {
                                if (throwable instanceof HttpException && ((HttpException) throwable).code() == 409) {
                                    //JSONObject jObjError = new JSONObject(((HttpException)throwable).response().errorBody().string());
                                    final Snackbar snackbar = Snackbar.make(getView(), "このグループを使用しているテンプレートがあるので、削除できません。", Snackbar.LENGTH_LONG);
                                    snackbar.getView().setBackgroundColor(Color.BLACK);
                                    snackbar.setActionTextColor(Color.WHITE);
                                    snackbar.show();
                                }
                            }
                        }
                ));
    }

}
