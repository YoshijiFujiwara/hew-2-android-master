package com.hew.second.gathering.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.SelectedSession;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SessionMainFragment extends Fragment {

    TextView session_site_tv, session_people_tv, session_time_tv, session_main_image_text_tv;
    Button session_finish_btn;

    public static SessionMainFragment newInstance() {
        return new SessionMainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            // todo とりあえず、セッションIDを１に設定
            SelectedSession.setSessionId(getActivity().getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE), 1);
            Toast.makeText(getActivity(), String.valueOf(SelectedSession.getSharedSessionId(getActivity().getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE))), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentActivity fragmentActivity = getActivity();
        View view = inflater.inflate(R.layout.fragment_event_finish, container, false);
        if (fragmentActivity != null) {
            (new BudgetFragment()).getSessionDetailFromSP(); // ダメダメコードだね
            Session session = SelectedSession.getSessionDetail(fragmentActivity.getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE));

            // TextViewにアイコンをつける
            session_site_tv = (TextView) view.findViewById(R.id.session_site);
            session_people_tv = (TextView) view.findViewById(R.id.session_people);
            session_time_tv = (TextView) view.findViewById(R.id.session_time);
            session_main_image_text_tv = (TextView) view.findViewById(R.id.session_main_image_text);

            String start = (session.start_time != null)? session.start_time : "";
            String end = (session.end_time != null)? session.end_time : "";
            session_site_tv.setText("店ID" + session.shop_id);
            session_time_tv.setText(start + "~" + end);
            session_people_tv.setText(String.valueOf(session.users.size()) + "人");
            session_main_image_text_tv.setText(session.name);

            session_site_tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location_on, 0, 0, 0);
            session_people_tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_people_black, 0, 0, 0);
            session_time_tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_access_time, 0, 0, 0);

            // セッション終了ボタン
            session_finish_btn = (Button) view.findViewById(R.id.session_finish_btn);
            session_finish_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog ad = new AlertDialog.Builder(getActivity()).create();
                    ad.setCancelable(false);
                    ad.setTitle("セッションを終了しますか？");
                    ad.setMessage(session.name + "\n" + start + "~" + end + "\n" + String.valueOf(session.users.size()) + "人\n");
                    ad.setButton("イベント終了", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // todo セッションの削除apiを叩く
                            endTheSession(fragmentActivity, session);
                        }
                    });
                    ad.setButton3("キャンセル", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    ad.show();
                }
            });
        }
        return view;
    }

    private void endTheSession(FragmentActivity fragmentActivity, Session session) {
        ApiService service = Util.getService();
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    HashMap<String, String> body = new HashMap<>();
                    body.put("name", session.name);
                    body.put("shop_id", session.shop_id);
                    body.put("budget", Integer.toString( session.budget));
                    body.put("actual", Integer.toString(session.actual));
                    body.put("start_time", session.start_time);
                    body.put("end_time", getNowDate()); // 現在の時間を入れる
                    // sharedPreferenceからセッションIDを取得する
                    return service.updateSession(LoginUser.getToken(),
                            SelectedSession.getSharedSessionId(fragmentActivity.getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE)),
                            body
                    );
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Util.setLoading(false, fragmentActivity);
                            Log.v("sessioninfo", list.data.name);

                            // sharedPreferenceにsessionの詳細情報を渡す
                            SelectedSession.setSessionDetail(fragmentActivity.getSharedPreferences(Util.PREF_FILE_NAME, Context.MODE_PRIVATE), list.data);
                            Toast.makeText(fragmentActivity, "セッションは終了しました", Toast.LENGTH_LONG).show();

                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            Toast.makeText(fragmentActivity, "セッション情報を更新できませんでした", Toast.LENGTH_LONG).show();
                        }
                );
    }

    public static String getNowDate(){
        final DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        final java.util.Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }
}
