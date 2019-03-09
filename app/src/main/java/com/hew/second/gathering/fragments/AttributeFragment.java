package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.AddMemberActivity;
import com.hew.second.gathering.activities.EditAttributeActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.activities.MemberDetailActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Attribute;
import com.hew.second.gathering.api.AttributeList;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.FriendList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.AttributeAdapter;
import com.hew.second.gathering.views.adapters.MemberAdapter;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static com.hew.second.gathering.activities.BaseActivity.INTENT_ATTRIBUTE_DETAIL;
import static com.hew.second.gathering.activities.BaseActivity.INTENT_FRIEND_DETAIL;

public class AttributeFragment extends BaseFragment {
    private static final String MESSAGE = "message";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AttributeAdapter adapter = null;
    private ArrayList<Attribute> ar = new ArrayList<>();
    private ListView listView = null;

    public static AttributeFragment newInstance(int kind) {
        AttributeFragment fragment = new AttributeFragment();

        Bundle args = new Bundle();
        args.putInt(MESSAGE, kind);
        fragment.setArguments(args);

        return fragment;
    }

    public static AttributeFragment newInstance() {
        return new AttributeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_attribute, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.setTitle("属性設定");

        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener((v) -> {
            Intent intent = new Intent(activity.getApplication(), EditAttributeActivity.class);
            startActivityForResult(intent, INTENT_ATTRIBUTE_DETAIL);
        });
        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        listView = activity.findViewById(R.id.attribute_list);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if(view.getId() == R.id.attribute_delete) {
                new MaterialDialog.Builder(activity)
                        .title("属性削除")
                        .content(ar.get(position).name + "を削除しますか？")
                        .positiveText("OK")
                        .onPositive((dialog, which) -> {
                            deleteAttribute(ar.get(position).id);
                        })
                        .negativeText("キャンセル")
                        .show();
            } else {
                Intent intent = new Intent(activity.getApplication(), EditAttributeActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("ATTRIBUTE_DETAIL", Parcels.wrap(ar.get(position)));
                intent.putExtras(bundle);
                startActivityForResult(intent, INTENT_ATTRIBUTE_DETAIL);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchList();
    }

    // Resumeの代わり
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }


    private void fetchList() {
        mSwipeRefreshLayout.setRefreshing(true);
        ApiService service = Util.getService();
        Observable<AttributeList> friendList = service.getAttributeList();
        cd.add(friendList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            if (activity != null && !cd.isDisposed()) {
                                updateList(list.data);
                            }
                        },  // 成功時
                        throwable -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                if (throwable instanceof NullPointerException) {
                                    final Snackbar snackbar = Snackbar.make(view, "データがありません。", Snackbar.LENGTH_LONG);
                                    snackbar.getView().setBackgroundColor(Color.BLACK);
                                    snackbar.setActionTextColor(Color.WHITE);
                                    snackbar.show();
                                } else if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    // ログインアクティビティへ遷移
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                ));
    }

    private void updateList(List<Attribute> data) {
        // ListView生成
        listView = activity.findViewById(R.id.attribute_list);
        if(listView != null) {
            ArrayList<Attribute> list = new ArrayList<>(data);
            ar = new ArrayList<>(data);
            adapter = new AttributeAdapter(list);
            if(listView != null){
                // ListViewにadapterをセット
                listView.setAdapter(adapter);
            }
        }
    }

    public void deleteAttribute(int id) {
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        ApiService service = Util.getService();
        Completable at = service.deleteAttribute(id);
        cd.add(at.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            if (activity != null) {
                                dialog.dismiss();
                                final Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "属性を削除しました。", Snackbar.LENGTH_SHORT);
                                snackbar.getView().setBackgroundColor(Color.BLACK);
                                snackbar.setActionTextColor(Color.WHITE);
                                snackbar.show();
                                fetchList();
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                dialog.dismiss();
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
