package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.gurunavi.GurunaviApiService;
import com.hew.second.gathering.gurunavi.GurunaviHttp;
import com.hew.second.gathering.gurunavi.Tel;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

// お店に予約の電話
public class ReservationPhoneFragment extends SessionBaseFragment {

    String tel;


    public static ReservationPhoneFragment newInstance() {
        Bundle args = new Bundle();
        ReservationPhoneFragment fragment = new ReservationPhoneFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_reservation_phone, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().setTitle("お店予約");
        TextView shopName = activity.findViewById(R.id.shop_name);

        if (activity.shop == null) {
            shopName.setText("未定");
        } else {
            shopName.setText(activity.shop.name);
        }
        TextView time = activity.findViewById(R.id.time);
        if (activity.session.start_time == null) {
            time.setText("未定");
        } else {
            time.setText(activity.session.start_time + "〜");
        }
        TextView number = activity.findViewById(R.id.number);
        number.setText(activity.session.users.size() + 1 + "人");

        Button button = activity.findViewById(R.id.reserve_button);
        button.setEnabled(false);

        if (activity.shop == null) {
            button.setText("店が選択されていません");
            return;
        }

        HashMap<String, String> body = new HashMap<>();
        String freeWord = activity.shop.name_kana.replace(" ", ",");
        body.put("freeword", freeWord);
        body.put("freeword_condition", "2");
        //String address = activity.shop.address;
        //body.put("address", address);
        body.put("latitude", activity.shop.lat);
        body.put("longitude", activity.shop.lng);
        body.put("range", "1");
        GurunaviApiService service = GurunaviHttp.getService();
        Observable<Tel> tel = service.getTel(body);
        cd.add(tel.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            TextView telName = activity.findViewById(R.id.tel_name);
                            telName.setText(list.rest.get(0).name);
                            button.setOnClickListener((l) -> {
                                Uri uri = Uri.parse("tel:" + list.rest.get(0).tel);
                                ;
                                if (!list.rest.get(0).tel_sub.isEmpty()) {
                                    uri = Uri.parse("tel:" + list.rest.get(0).tel_sub);
                                }
                                Intent i = new Intent(Intent.ACTION_DIAL, uri);
                                startActivity(i);
                            });
                            button.setEnabled(true);
                        },  // 成功時
                        throwable ->
                        {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    // ログインアクティビティへ遷移
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                } else {
                                    button.setOnClickListener((l) -> {
                                        final CustomTabsIntent tabsIntent = new CustomTabsIntent.Builder()
                                                .setShowTitle(true)
                                                .setToolbarColor(ContextCompat.getColor(activity.getApplication(), R.color.colorPrimary))
                                                .setStartAnimations(activity.getApplication(), R.anim.slide_in_right, R.anim.slide_out_left)
                                                .setExitAnimations(activity.getApplication(), android.R.anim.slide_in_left, android.R.anim.slide_out_right).build();
                                        Uri uri = Uri.parse(activity.shop.urls.pc);
                                        tabsIntent.launchUrl(activity, uri);
                                    });
                                    button.setText("Webサイトから予約");
                                    button.setEnabled(true);
                                }
                            }
                        }
                ));

    }
}
