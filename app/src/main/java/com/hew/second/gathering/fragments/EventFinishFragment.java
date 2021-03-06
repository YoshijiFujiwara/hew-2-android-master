package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.InputType;
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
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.SessionUser;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.hotpepper.GourmetResult;
import com.hew.second.gathering.hotpepper.HpApiService;
import com.hew.second.gathering.hotpepper.HpHttp;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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
                new MaterialDialog.Builder(activity)
                        .title(activity.session.name)
                        .content("イベントを終了しますか？")
                        .positiveText("OK")
                        .onPositive((dialog, which) -> {
                            finishSession();
                        })
                        .negativeText("キャンセル")
                        .show();
            });

            BottomNavigationView bnv = activity.findViewById(R.id.eip_bottom_navigation);

            TextView sessionTitle = activity.findViewById(R.id.session_main_image_text);
            sessionTitle.setOnClickListener((l) -> {
                new MaterialDialog.Builder(activity)
                        .title("イベント名")
                        .content("タイトル")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .inputRangeRes(1, 30, R.color.colorAccentDark)
                        .input("イベント名", activity.session.name, (MaterialDialog dialog, CharSequence input) -> {
                            updateSessionName(input.toString());
                        })
                        .negativeText("キャンセル")
                        .show();
            });

            CardView placeIcon = activity.findViewById(R.id.cardView5);
            placeIcon.setOnClickListener((l) -> {
                if (activity != null) {
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    if (fragmentManager != null) {
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.eip_container, EditShopFragment.newInstance());
                        fragmentTransaction.commit();
                    }
                }
            });


            CardView timeIcon = activity.findViewById(R.id.cardView6);
            timeIcon.setOnClickListener((l) -> {
                bnv.setSelectedItemId(R.id.navi_botto_time);
                if (activity != null) {
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    if (fragmentManager != null) {
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        //fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.eip_container, StartTimeFragment.newInstance());
                        fragmentTransaction.commit();
                    }
                }
            });


            CardView inviteIcon = activity.findViewById(R.id.cardView3);
            inviteIcon.setOnClickListener((l) -> {
                bnv.setSelectedItemId(R.id.navi_botto_member);
                if (activity != null) {
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();
                    if (fragmentManager != null) {
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        //fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.replace(R.id.eip_container, InviteFragment.newInstance());
                        fragmentTransaction.commit();
                    }
                }
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

        // 時刻表示フォーマット
        TextView time = activity.findViewById(R.id.time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat output = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        SimpleDateFormat outputShort = new SimpleDateFormat("HH:mm", Locale.getDefault());
        StringBuilder strTime = new StringBuilder();
        if (activity.session.start_time != null) {
            try {
                Date start = sdf.parse(activity.session.start_time);
                strTime.append(output.format(start));
                strTime.append(" 〜 ");
                if (activity.session.end_time != null) {
                    Date end = sdf.parse(activity.session.end_time);
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
        time.setText(strTime.toString());

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
        address.setOnClickListener((l) -> {
            Uri uri = Uri.parse("geo:<" + activity.shop.lat + ">,<" + activity.shop.lng + ">?q="+ activity.shop.name);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        NumberFormat nfCur = NumberFormat.getCurrencyInstance();
        TextView budget = activity.findViewById(R.id.textView_budget);
        if (activity.session.budget == 0) {
            budget.setText("未定");
        } else {
            budget.setText(nfCur.format(activity.session.budget) + " /人");
        }
        TextView access = activity.findViewById(R.id.textView_access);
        access.setText(activity.shop.mobile_access);
        TextView url = activity.findViewById(R.id.textView_url);
        url.setText(activity.shop.urls.pc);
        url.setTextColor(getResources().getColor(R.color.colorPrimary));
        url.setOnClickListener((l) -> {
            final CustomTabsIntent tabsIntent = new CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setToolbarColor(ContextCompat.getColor(activity.getApplication(), R.color.colorPrimary))
                    .setStartAnimations(activity.getApplication(), R.anim.slide_in_right, R.anim.slide_out_left)
                    .setExitAnimations(activity.getApplication(), android.R.anim.slide_in_left, android.R.anim.slide_out_right).build();
            Uri uri = Uri.parse(url.getText().toString());
            tabsIntent.launchUrl(activity, uri);
        });
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
        Completable session = Util.getService().deleteSession(activity.session.id);
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

    private void updateSessionName(String name) {
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();

        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("name", name);
        Observable<SessionDetail> sessionDetail = service.updateSession(activity.session.id, body);
        cd.add(sessionDetail.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        (list) -> {
                            if (dialog != null && activity != null) {
                                activity.setTitle(list.data.name);
                                TextView title = activity.findViewById(R.id.session_main_image_text);
                                title.setText(list.data.name);
                                activity.session = list.data;
                                dialog.dismiss();
                                final Snackbar snackbar = Snackbar.make(view, "イベント名を更新しました。", Snackbar.LENGTH_SHORT);
                                snackbar.getView().setBackgroundColor(Color.BLACK);
                                snackbar.setActionTextColor(Color.WHITE);
                                snackbar.show();
                            }
                        },
                        (throwable) -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (dialog != null) {
                                dialog.dismiss();
                            }
                            if (activity != null && !cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }));

    }

}
