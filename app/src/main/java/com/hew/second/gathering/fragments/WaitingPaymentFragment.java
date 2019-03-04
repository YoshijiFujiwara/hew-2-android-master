package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.EventProcessMainActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.hotpepper.GourmetResult;
import com.hew.second.gathering.hotpepper.HpApiService;
import com.hew.second.gathering.hotpepper.HpHttp;
import com.hew.second.gathering.hotpepper.Shop;
import com.hew.second.gathering.views.adapters.SessionAdapter;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static com.hew.second.gathering.R.layout;
import static io.reactivex.Observable.concat;

//セッション一覧支払い待ち
public class WaitingPaymentFragment extends BaseFragment {
    private static final String MESSAGE = "message";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SessionAdapter adapter = null;
    private ArrayList<Session> ar = new ArrayList<>();
    private ArrayList<Shop> shopList = new ArrayList<>();
    private ArrayList<String> headers = new ArrayList<>();
    private ListView listView = null;

    public static WaitingPaymentFragment newInstance() {
        WaitingPaymentFragment fragment = new WaitingPaymentFragment();
        return fragment;
    }

    @Override
    public void onDestroy() {
        cd.clear();
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(layout.fragment_waiting_payment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout_waiting_payment);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        listView = activity.findViewById(R.id.listView_waitingPay);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(activity, EventProcessMainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("SHOP_DETAIL", Parcels.wrap(shopList.get(position)));
            bundle.putParcelable("SESSION_DETAIL", Parcels.wrap(ar.get(position)));
            intent.putExtras(bundle);
            startActivity(intent);
        });

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        activity.setTitle("イベント一覧");
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchList();
    }

    private void fetchList() {
        mSwipeRefreshLayout.setRefreshing(true);
        ApiService service = Util.getService();
        HpApiService hpService = HpHttp.getService();
        Observable<SessionList> token = service.getSessionNotPaymentComplete(LoginUser.getToken());
        List<Shop> shops = new ArrayList<>();
        cd.add(token.subscribeOn(Schedulers.io())
                .flatMap((list) -> {
                    // 支払い待ち
                    ar = new ArrayList<>(list.data);
                    headers = new ArrayList<>();
                    for (Session s : list.data) {
                        headers.add("支払い待ちのイベント");
                    }
                    StringBuilder strId = new StringBuilder();
                    String prefix = "";
                    ArrayList<String> strList = new ArrayList<>();
                    List<Observable<GourmetResult>> l = new ArrayList<>();
                    for (Session s : ar) {
                        if (!strList.contains(s.shop_id)) {
                            strList.add(s.shop_id);
                            strId.append(prefix);
                            prefix = ",";
                            strId.append(s.shop_id);

                            if (strList.size() % 20 == 0) {
                                Map<String, String> body = new HashMap<>();
                                body.put("id", strId.toString());
                                l.add(hpService.getShopList(body));
                                prefix = "";
                                strId = new StringBuilder();
                            }
                        }
                    }
                    if (!strId.toString().isEmpty()) {
                        Map<String, String> body = new HashMap<>();
                        body.put("id", strId.toString());
                        l.add(hpService.getShopList(body));
                    }
                    return concat(l);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            shops.addAll(list.results.shop);
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
                        },
                        () -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            //画像後更新
                            updateList(ar, shops);

                        }
                ));
    }

    private void updateList(List<Session> data, List<Shop> shops) {
        // ListView生成
        listView = activity.findViewById(R.id.listView_waitingPay);
        ArrayList<Session> list = new ArrayList<>(data);
        ar = new ArrayList<>(data);
        // ショップリストをセッションと同一形式にする
        ArrayList<Shop> shopSession = new ArrayList<>();
        for (Session s : data) {
            for (Shop shop : shops) {
                if (s.shop_id.equals(shop.id)) {
                    shopSession.add(shop);
                    break;
                }
            }
        }
        shopList = new ArrayList<>(shopSession);
        adapter = new SessionAdapter(list, shopSession, headers);
        if (listView != null) {
            // ListViewにadapterをセット
            listView.setAdapter(adapter);
        }
    }
}
