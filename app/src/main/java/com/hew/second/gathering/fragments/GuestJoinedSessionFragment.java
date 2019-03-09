package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.GuestSessionDetailActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.hotpepper.HpApiService;
import com.hew.second.gathering.hotpepper.HpHttp;
import com.hew.second.gathering.hotpepper.Shop;
import com.hew.second.gathering.views.adapters.GuestSessionAdapter;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static com.hew.second.gathering.activities.BaseActivity.INTENT_GUEST_SESSION_DETAIL;

public class GuestJoinedSessionFragment extends BaseFragment {
    private static final String MESSAGE = "message";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GuestSessionAdapter adapter = null;
    private ArrayList<Session> ar = new ArrayList<>();
    private ArrayList<Shop> shopList = new ArrayList<>();
    private ListView listView = null;

    public static GuestJoinedSessionFragment newInstance() {
        return new GuestJoinedSessionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_guest_joined, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout_guest_joined);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        listView = activity.findViewById(R.id.listView_guest_joined);
        listView.setEmptyView(activity.findViewById(R.id.emptyView_guest_joined));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(activity, GuestSessionDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("SESSION_DETAIL", Parcels.wrap(ar.get(position)));
            bundle.putParcelable("SHOP_DETAIL", Parcels.wrap(shopList.get(position)));
            bundle.putString("STATUS", "ALLOW");
            intent.putExtras(bundle);
            startActivityForResult(intent,INTENT_GUEST_SESSION_DETAIL);
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
        HpApiService hpService = HpHttp.getService();
        Observable<SessionList> sessionList = service.getGuestSessionAllowList();
        cd.add(sessionList.subscribeOn(Schedulers.io())
                .flatMap((list) -> {
                    ar = new ArrayList<>(list.data);
                    StringBuilder strId = new StringBuilder();
                    String prefix = "";
                    for (Session s : list.data) {
                        strId.append(prefix);
                        prefix = ",";
                        strId.append(s.shop_id);
                    }
                    Map<String, String> body = new HashMap<>();
                    body.put("id", strId.toString());
                    return hpService.getShopList(body);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            //画像後更新
                            updateList(ar, list.results.shop);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            mSwipeRefreshLayout.setRefreshing(false);
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

    private void updateList(List<Session> data, List<Shop> shops) {
        // ListView生成
        listView = activity.findViewById(R.id.listView_guest_joined);
        ArrayList<Session> list = new ArrayList<>(data);
        ar = new ArrayList<>(data);
        // ショップリストをセッションと同一形式にする
        ArrayList<Shop> shopSession = new ArrayList<>();
        for(Session s :data){
            for(Shop shop : shops)
            {
                if(s.shop_id.equals(shop.id)){
                    shopSession.add(shop);
                    break;
                }
            }
        }
        shopList = new ArrayList<>(shopSession);
        adapter = new GuestSessionAdapter(list, shopSession);
        if(listView != null){
            listView.setAdapter(adapter);
        }
    }

}
