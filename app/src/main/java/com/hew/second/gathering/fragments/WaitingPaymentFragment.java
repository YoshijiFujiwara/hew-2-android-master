package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.EventProcessMainActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.activities.MainActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.SessionList;
import com.hew.second.gathering.api.SessionUser;
import com.hew.second.gathering.api.SessionUserList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.hotpepper.GourmetResult;
import com.hew.second.gathering.hotpepper.HpApiService;
import com.hew.second.gathering.hotpepper.HpHttp;
import com.hew.second.gathering.hotpepper.Shop;
import com.hew.second.gathering.views.adapters.SessionAdapter;

import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static com.hew.second.gathering.R.layout;
import static com.hew.second.gathering.activities.BaseActivity.SNACK_MESSAGE;
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
        listView.setEmptyView(activity.findViewById(R.id.emptyView_waiting_payment));
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(activity, EventProcessMainActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("SHOP_DETAIL", Parcels.wrap(shopList.get(position)));
            bundle.putParcelable("SESSION_DETAIL", Parcels.wrap(ar.get(position)));
            intent.putExtras(bundle);
            startActivity(intent);
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat output = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        SimpleDateFormat outputShort = new SimpleDateFormat("HH:mm", Locale.getDefault());

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            final ArrayList<String> list = new ArrayList<>();
            list.add("同じ設定のイベントを新規作成");
            list.add("イベントを終了");

            StringBuilder strTime = new StringBuilder();
            String strStartTime = ar.get(position).start_time;
            String strEndTime = ar.get(position).end_time;

            if (strStartTime != null) {
                try {
                    Date start = sdf.parse(strStartTime);
                    strTime.append(output.format(start));
                    strTime.append("〜");
                    if (strEndTime != null) {
                        Date end = sdf.parse(strEndTime);
                        if (dateOnly.format(start).equals(dateOnly.format(end))) {
                            strTime.append(outputShort.format(end));
                        } else {
                            strTime.append(output.format(end));
                        }
                    } else {
                        strTime.append("未定");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                strTime.append("未定");
            }

            new MaterialDialog.Builder(activity)
                    .title(ar.get(position).name)
                    .content(shopList.get(position).name + "\n" + strTime.toString())
                    .items(list)
                    .itemsColor(getResources().getColor(R.color.colorPrimary))
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                            if (text.equals("同じ設定のイベントを新規作成")) {
                                new MaterialDialog.Builder(activity)
                                        .title("イベント作成")
                                        .content("タイトル")
                                        .inputType(InputType.TYPE_CLASS_TEXT)
                                        .inputRangeRes(1, 30, R.color.colorAccentDark)
                                        .input("新規イベント名", ar.get(position).name, (MaterialDialog d, CharSequence input) -> {
                                            createCopySession(input.toString(), ar.get(position), shopList.get(position));
                                        })
                                        .negativeText("キャンセル")
                                        .show();
                            } else if (text.equals("イベントを終了")) {
                                new MaterialDialog.Builder(activity)
                                        .title("イベント")
                                        .content(ar.get(position).name + "を終了しますか？")
                                        .positiveText("OK")
                                        .onPositive((d, w) -> {
                                            finishSession(ar.get(position).id);
                                        })
                                        .negativeText("キャンセル")
                                        .show();
                            }
                        }
                    })
                    .negativeText("閉じる")
                    .show();
            return true;

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
        Observable<SessionList> token = service.getSessionNotPaymentComplete();
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

    private void createCopySession(String title, Session originalSession, Shop shop) {

        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();

        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("name", title);
        body.put("shop_id", shop.id);
        body.put("budget",String.valueOf(originalSession.budget));
        Observable<SessionDetail> session = service.createSession(body);
        final Session[] s = new Session[1];
        cd.add(session.subscribeOn(Schedulers.io())
                .flatMap((newSession) -> {
                    s[0] = newSession.data;
                    ArrayList<Observable<SessionUserList>> inviteList = new ArrayList<>();
                    for (SessionUser user : originalSession.users) {
                        HashMap<String, String> param = new HashMap<>();
                        param.put("user_id", String.valueOf(user.id));
                        inviteList.add(service.createSessionUser(newSession.data.id, param));
                    }

                    return Observable.concat(inviteList);

                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        (list) -> {
                            //遷移

                        },
                        (throwable) -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (!cd.isDisposed() && activity != null) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                            dialog.dismiss();
                        },
                        () -> {
                            if (activity != null) {
                                dialog.dismiss();
                                Intent intent = new Intent(activity.getApplication(), EventProcessMainActivity.class);
                                intent.putExtra(SNACK_MESSAGE, "イベントを作成しました。");
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("SHOP_DETAIL", Parcels.wrap(shop));
                                bundle.putParcelable("SESSION_DETAIL", Parcels.wrap(s[0]));
                                intent.putExtras(bundle);
                                activity.setResult(activity.RESULT_OK, intent);
                                startActivity(intent);
                            }
                        }
                ));
    }

    private void finishSession(int id) {
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        Completable session = Util.getService().deleteSession(id);
        cd.add(session
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            if (activity != null) {
                                dialog.dismiss();
                                fetchList();
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (!cd.isDisposed() && activity != null) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    // ログインアクティビティへ遷移
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                                dialog.dismiss();
                            }
                        }
                ));
    }
}
