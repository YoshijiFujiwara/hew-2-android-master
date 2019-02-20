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

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.EditGroupActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.FriendList;
import com.hew.second.gathering.api.Group;
import com.hew.second.gathering.api.GroupDetail;
import com.hew.second.gathering.api.GroupList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.hotpepper.GourmetResult;
import com.hew.second.gathering.hotpepper.HpHttp;
import com.hew.second.gathering.views.adapters.GroupAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static com.hew.second.gathering.activities.BaseActivity.INTENT_EDIT_GROUP;

public class SearchShopFragment extends BaseFragment {
    ArrayList<GroupAdapter.Data> ar = new ArrayList<GroupAdapter.Data>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    GroupAdapter adapter = null;
    private CompositeDisposable cd = new CompositeDisposable();

    public static SearchShopFragment newInstance() {
        return new SearchShopFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_search_shop,
                container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SlidingUpPanelLayout sup = activity.findViewById(R.id.sliding_layout_search);
        sup.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

        HashMap<String,String> options = new HashMap<>();
        options.put("keyword","a");
        Observable<GourmetResult> ShopList = HpHttp.getService().getShopList(options);
        cd.add(ShopList.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if(activity != null){

                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if(activity != null && !cd.isDisposed()){
                                if (throwable instanceof HttpException && ((HttpException) throwable).code() == 401) {
                                    // ログインアクティビティへ遷移
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                ));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
