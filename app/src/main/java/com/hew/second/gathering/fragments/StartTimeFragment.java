package com.hew.second.gathering.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.Util;

import java.security.cert.Extension;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

//　開始時刻設定
public class StartTimeFragment extends SessionBaseFragment {

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";
    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";

    //      現在 年 月 日 時 分
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int monthOfYear = calendar.get(Calendar.MONTH);
    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);
    int second = calendar.get(Calendar.SECOND);
    int endYear = calendar.get(Calendar.YEAR);
    int endMonthOfYear = calendar.get(Calendar.MONTH);
    int endDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    int endHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
    int endMinute = calendar.get(Calendar.MINUTE);
    int endSecond = calendar.get(Calendar.SECOND);

    String strStartTime;
    String strEndTime;


    public static StartTimeFragment newInstance() {

        Bundle args = new Bundle();
        StartTimeFragment fragment = new StartTimeFragment();
        fragment.setArguments(args);
        return fragment;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_starttime, container, false);

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity.setTitle("イベント時間設定");
        activity.fragment = "TIME";

        TextView shopName = activity.findViewById(R.id.st_location);
        if (activity.shop == null) {
            shopName.setText("未定");
        } else {
            shopName.setText(activity.shop.name);
        }
        TextView time = activity.findViewById(R.id.st_date);
        if (activity.session.start_time == null) {
            time.setText("未定");
        } else {
            time.setText(activity.session.start_time + "〜");
        }
        TextView number = activity.findViewById(R.id.st_number);
        number.setText(activity.session.users.size() + 1 + "人");

        TextView startDateText = activity.findViewById(R.id.start_date);
        TextView startTimeText = activity.findViewById(R.id.start_timer);
        TextView endDateText = activity.findViewById(R.id.end_date);
        TextView endTimeText = activity.findViewById(R.id.end_timer);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy年MM月dd日(E)");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH時mm分");
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN);

//      TextViewの初期表示は開始時刻は現在時刻と現在時刻に一時間加算して表示
        startDateText.setText("未定");
        startTimeText.setText("未定");
        endDateText.setText("未定");
        endTimeText.setText("未定");

//      開始時間が設定されていたら
        if (activity.session.start_time != null) {
//          Stringの日付をフォーマットしDateクラスに
            strStartTime = activity.session.start_time;
            try {
                Date date = sdFormat.parse(activity.session.start_time);

                calendar.setTime(date);
                year = calendar.get(Calendar.YEAR);
                monthOfYear = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);

                // 開始時間のTextView表示
                startDateText.setText(sdfDate.format(date));
                startTimeText.setText(sdfTime.format(date));

                // 終了時間仮設定
                endDateText.setText(sdfDate.format(date));
                calendar.add(Calendar.HOUR_OF_DAY , 1);
                endTimeText.setText(sdfTime.format(calendar.getTime()));

                endYear = calendar.get(Calendar.YEAR);
                endMonthOfYear = calendar.get(Calendar.MONTH);
                endDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                endHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                endMinute = calendar.get(Calendar.MINUTE);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            //strStartTime = sdFormat.format(calendar.getTime());
            strStartTime = "未定";
        }
        // 終了時間が設定されていたら
        if (activity.session.end_time != null) {
            strEndTime = activity.session.end_time;

            try {
                Date date = sdFormat.parse(activity.session.end_time);

//                終了時間のTextView表示
                endDateText.setText(sdfDate.format(date));
                endTimeText.setText(sdfTime.format(date));
//
                calendar.setTime(date);
                endYear = calendar.get(Calendar.YEAR);
                endMonthOfYear = calendar.get(Calendar.MONTH);
                endDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                endHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                endMinute = calendar.get(Calendar.MINUTE);

            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
//          設定なし（値がなかった）の場合
            //strEndTime = sdFormat.format(addHour(1).getTime());
            endDateText.setAlpha(0.5f);
            endTimeText.setAlpha(0.5f);
            strEndTime = "未定";
        }

//            第1 context 第2 日付が選択された時のコールバック 第3 年の値 第4 月の値 第5 日の値
        DatePickerDialog startDate = new DatePickerDialog(activity, new DateSetHandler(startDateText), year, monthOfYear, dayOfMonth);
        TimePickerDialog startTime = new TimePickerDialog(activity, new DateSetHandler(startTimeText), hourOfDay, minute, true);
        DatePickerDialog endDate = new DatePickerDialog(activity, new DateSetHandler(endDateText), endYear, endMonthOfYear, endDayOfMonth);
        TimePickerDialog endTime = new TimePickerDialog(activity, new DateSetHandler(endTimeText), endHourOfDay, endMinute, true);

//              開始日付のTextViewがクリックされた時
        startDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDate.show();

            }
        });
//               開始時間のTextViewがクリックされた時
        startTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime.show();
            }
        });
//              終了日付のTextViewがクリックされた時
        endDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endDate.show();
            }
        });
//              終了時間のTextViewがクリックされた時
        endTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTime.show();
            }
        });

        Button reserveButton = activity.findViewById(R.id.reserve_button);
        final Calendar[] setStartCalender = new Calendar[1];
        final Calendar[] setEndCalender = new Calendar[1];

        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Snackbar snackbar = Snackbar.make(view, "開始時刻と終了時刻の値が不正です。", Snackbar.LENGTH_SHORT);
                snackbar.getView().setBackgroundColor(Color.BLACK);
                snackbar.setActionTextColor(Color.WHITE);
//                一応チェック
                if (startDate.onSaveInstanceState() != null && startTime.onSaveInstanceState() != null) {

                    setStartCalender[0] = setCalenderOfDate(startDate, startTime);
                    strStartTime = (String) DateFormat.format("yyyy-MM-dd HH:mm:ss", setStartCalender[0]);
                }

                if (endDate.onSaveInstanceState() != null && endTime.onSaveInstanceState() != null) {
                    setEndCalender[0] = setCalenderOfDate(endDate, endTime);
                    strEndTime = (String) DateFormat.format("yyyy-MM-dd HH:mm:ss", setEndCalender[0]);
                }


                TextView startDateText = activity.findViewById(R.id.start_date);
                TextView startTimeText = activity.findViewById(R.id.start_timer);
                TextView endDateText = activity.findViewById(R.id.end_date);
                TextView endTimeText = activity.findViewById(R.id.end_timer);
                if (startDateText.getText().equals("未定") || startTimeText.getText().equals("未定")){
                    strStartTime = null;
                }
                if(endDateText.getText().equals("未定") || endTimeText.getText().equals("未定")) {
                    strEndTime = null;
                }

//              日付比較　０
                int diff = setStartCalender[0].compareTo(setEndCalender[0]);


                if (diff == 0 && strEndTime != null) {
                    Log.d("calenderComparison", "TheSame 同じ");
                    snackbar.show();
                } else if (diff > 0 && strEndTime != null) {
                    Log.d("calenderComparison", "開始時間のほうが終了時間より先に進んでいます");
                    snackbar.show();

                } else {
                    Log.d("calenderComparison", "開始時間は終了時間より過去です");
                    endDateText.setAlpha(1.0f);
                    endTimeText.setAlpha(1.0f);
                    updateDate(activity.session, strStartTime, strEndTime);
                }
            }
        });


    }

    //      日付・時刻選択された時TextViewに格納
    private class DateSetHandler implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

        private TextView textView;
        private Calendar calendar = Calendar.getInstance();

        public DateSetHandler(TextView textView) {
            this.textView = textView;
        }

        //      日付が選択された時の処理
//       Calender.set(第１,第２)
//       第１引数 指定したいカレンダーフィールド
//       第２引数 指定されたカレンダーフィールドに設定したい値
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String text = (String) DateFormat.format("yyyy年MM月dd日", calendar);
            textView.setText(text);

        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);

            String text = (String) DateFormat.format("HH時mm分", calendar);
            textView.setText(text);
        }
    }


    /**
     * 現在の日付・時刻から指定の【時間】を加算・減算した結果を返します。
     *
     * @param addHour 加算・減算する時間
     * @return 計算後の Calendar インスタンス。
     */
    public static Calendar addHour(int addHour) {
        return add(null, 0, 0, 0, addHour, 0, 0, 0);
    }

    /**
     * 各時間フィールドに設定する数量が0の場合は、現在の値が設定されます。
     * java.util.GregorianCalendarの内部処理では以下の分岐を行っている。
     * if (amount == 0) {
     * return;
     * }
     *
     * @param cal     日付時刻の指定があればセットする。
     *                nullの場合、現在の日付時刻で新しいCalendarインスタンスを生成する。
     * @param addHour 加算・減算する時間
     */
    public static Calendar add(Calendar cal,
                               int addYera, int addMonth, int addDate,
                               int addHour, int addMinute, int addSecond,
                               int addMillisecond) {
        if (cal == null) {
            cal = Calendar.getInstance();
        }
        cal.add(Calendar.HOUR_OF_DAY, addHour);
        return cal;
    }

    public void updateDate(Session session, String startTime, String endTime) {
        dialog = new SpotsDialog.Builder().setContext(activity).build();
        dialog.show();
        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("start_time", startTime);
        body.put("end_time", endTime);
        Observable<SessionDetail> token = service.updateSession(LoginUser.getToken(), session.id, body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (activity != null) {
                                activity.session.start_time = list.data.start_time;
                                activity.session.end_time = list.data.end_time;

                                dialog.dismiss();
                                final Snackbar snackbar = Snackbar.make(view, "時刻を更新しました。", Snackbar.LENGTH_SHORT);
                                snackbar.getView().setBackgroundColor(Color.BLACK);
                                snackbar.setActionTextColor(Color.WHITE);
                                snackbar.show();
                            }

                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            dialog.dismiss();
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

    //   設定された日付と時間を取得してCalenderオブジェクトにセットしCalenderオブジェクトを返してます
    public Calendar setCalenderOfDate(DatePickerDialog datePickerDialog, TimePickerDialog timePickerDialog) {

        Calendar calendar = Calendar.getInstance();
//         日付 dataPickerDialog
        year = datePickerDialog.onSaveInstanceState().getInt(YEAR);
        monthOfYear = datePickerDialog.onSaveInstanceState().getInt(MONTH);
        dayOfMonth = datePickerDialog.onSaveInstanceState().getInt(DAY);
//         時間 timePickerDialog
        hourOfDay = timePickerDialog.onSaveInstanceState().getInt(HOUR);
        minute = timePickerDialog.onSaveInstanceState().getInt(MINUTE);
        second = timePickerDialog.onSaveInstanceState().getInt(IS_24_HOUR);

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        return calendar;
    }
}
