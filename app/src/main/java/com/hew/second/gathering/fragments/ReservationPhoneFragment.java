package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.FriendList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.hotpepper.GurunaviApiService;
import com.hew.second.gathering.hotpepper.GurunaviHttp;
import com.hew.second.gathering.hotpepper.Tel;

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

        TextView shopName = activity.findViewById(R.id.shop_name);
        shopName.setText(activity.shop.name);
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

        GurunaviApiService service = GurunaviHttp.getService();
        Observable<Tel> tel = service.getTel(activity.shop.name);
        cd.add(tel.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            TextView telName = activity.findViewById(R.id.tel_name);
                            telName.setText(list.rest.get(0).name);
                            button.setOnClickListener((l) -> {
                                Uri uri = Uri.parse("tel:" + list.rest.get(0).tel);
                                Intent i = new Intent(Intent.ACTION_DIAL, uri);
                                startActivity(i);
                            });
                            button.setEnabled(true);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
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
}
