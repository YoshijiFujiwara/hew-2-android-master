package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.activities.MainActivity;
import com.hew.second.gathering.api.SessionUser;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.hotpepper.GourmetResult;
import com.hew.second.gathering.hotpepper.HpApiService;
import com.hew.second.gathering.hotpepper.HpHttp;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static com.hew.second.gathering.activities.BaseActivity.SNACK_MESSAGE;

public class EventFinishFragment extends SessionBaseFragment {

    public EventFinishFragment() {
    }


    public static EventFinishFragment newInstance() {
        return new EventFinishFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_event_finish, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (activity.session != null) {
            activity.setTitle(activity.session.name);
            activity.fragment = null;

            Button button = activity.findViewById(R.id.session_finish_btn);
            button.setOnClickListener((l) -> {
                // 参加しない
                new MaterialDialog.Builder(activity)
                        .title("イベント")
                        .content(activity.session.name + "を終了しますか？")
                        .positiveText("OK")
                        .onPositive((dialog, which) -> {
                            finishSession();
                        })
                        .negativeText("キャンセル")
                        .show();
            });

            if (activity.shop == null) {
                fetchShop();
            } else {
                displayShopInfo();
            }
        }
    }

    private void displayShopInfo() {
        ImageView imageView = activity.findViewById(R.id.going_event);
        Picasso.get()
                .load(activity.shop.photo.pc.l)
                .fit()
                .centerInside()
                .into(imageView);

        TextView title = activity.findViewById(R.id.session_main_image_text);
        title.setText(activity.session.name);

        TextView shopName = activity.findViewById(R.id.shop_name);
        shopName.setText(activity.shop.name);
        TextView time = activity.findViewById(R.id.time);
        time.setText(activity.session.start_time + "〜" + activity.session.end_time);
        TextView countMember = activity.findViewById(R.id.count_member);
        int ok = 0;
        for (SessionUser u : activity.session.users) {
            if (u.join_status.equals("allow")) {
                ok++;
            }
        }
        countMember.setText(Integer.toString(ok + 1) + " / " + (activity.session.users.size() + 1) + "人");

        TextView genre = activity.findViewById(R.id.textView_genre);
        genre.setText(activity.shop.genre.name);
        TextView address = activity.findViewById(R.id.textView_address);
        address.setText(activity.shop.address);
        TextView budget = activity.findViewById(R.id.textView_budget);
        budget.setText(Integer.toString(activity.session.budget));
        TextView access = activity.findViewById(R.id.textView_access);
        access.setText(activity.shop.mobile_access);
        TextView url = activity.findViewById(R.id.textView_url);
        url.setText(activity.shop.urls.pc);
        url.setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    private void fetchShop() {
        if (activity.session.shop_id != null) {
            dialog = new SpotsDialog.Builder().setContext(activity).build();
            dialog.show();
            HpApiService hpService = HpHttp.getService();
            Map<String, String> body = new HashMap<>();
            body.put("id", activity.session.shop_id);
            Observable<GourmetResult> shopList = hpService.getShopList(body);
            cd.add(shopList
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe(
                            list -> {
                                activity.shop = list.results.shop.get(0);
                                displayShopInfo();
                                dialog.dismiss();
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

    private void finishSession() {
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        Completable session = Util.getService().deleteSession(LoginUser.getToken(), activity.session.id);
        cd.add(session
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            if (activity != null) {
                                dialog.dismiss();
                                Intent intent = new Intent(activity.getApplication(), MainActivity.class);
                                intent.putExtra(SNACK_MESSAGE, "イベントを完了しました。");
                                startActivity(intent);
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
