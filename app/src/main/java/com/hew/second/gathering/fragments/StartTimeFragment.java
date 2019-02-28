package com.hew.second.gathering.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static android.icu.text.DateFormat.DAY;
import static android.icu.text.DateFormat.HOUR;
import static android.icu.text.DateFormat.MINUTE;
import static android.icu.text.DateFormat.MONTH;
import static android.icu.text.DateFormat.YEAR;

//　開始時刻設定
public class StartTimeFragment extends SessionBaseFragment {


    //      現在 年 月 日 時 分
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int monthOfYear = calendar.get(Calendar.MONTH);
    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
    int minute = calendar.get(Calendar.MINUTE);
    String sStartTime ;
    String sEndTime;

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

        activity.setTitle("イベント時刻設定");

        TextView startDateText = getActivity().findViewById(R.id.start_date);
        TextView startTimeText = getActivity().findViewById(R.id.start_timer);
        TextView endDateText = getActivity().findViewById(R.id.end_date);
        TextView endTimeText = getActivity().findViewById(R.id.end_timer);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy年MM月dd日(E)");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH時mm分");
        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.JAPAN);

//          現在時刻と現在時刻に＋１された時間設定
        String sStartTime = sdFormat.format(calendar.getTime());
        String sEndTime = sdFormat.format(addHour(1).getTime());

        startDateText.setText(sdfDate.format(calendar.getTime()));
        startTimeText.setText(sdfTime.format(calendar.getTime()));
        endDateText.setText(sdfDate.format(calendar.getTime()));
        endTimeText.setText(sdfTime.format(addHour(1).getTime()));


        if (activity.session.start_time != null) {
//          Stringの日付をフォーマットしDateクラスに
             sStartTime = activity.session.start_time;
            try {
               Date date = sdFormat.parse(activity.session.start_time);
               startDateText.setText(sdfDate.format(date));
               startTimeText.setText(sdfTime.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else if (activity.session.end_time != null) {
            sEndTime = activity.session.end_time;
            try {
                Date date = sdFormat.parse(activity.session.end_time);
                endDateText.setText(sdfDate.format(date));
                endTimeText.setText(sdfTime.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


//            第1 context 第2 日付が選択された時のコールバック 第3 年の値 第4 月の値 第5 日の値
        DatePickerDialog startDate = new DatePickerDialog(getActivity(), new DateSetHandler(startDateText), year, monthOfYear, dayOfMonth);
        TimePickerDialog startTime = new TimePickerDialog(getActivity(), new DateSetHandler(startTimeText), hourOfDay, minute, true);
        DatePickerDialog endDate = new DatePickerDialog(getActivity(), new DateSetHandler(endDateText), year, monthOfYear, dayOfMonth);
        TimePickerDialog endTime = new TimePickerDialog(getActivity(), new DateSetHandler(endTimeText), hourOfDay, minute, true);


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


        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalender = Calendar.getInstance();
        Button reserveButton = getActivity().findViewById(R.id.reserve_button);

        if (startDate.onSaveInstanceState() != null) {

            int year = startDate.onSaveInstanceState().getInt(YEAR);
            int month= startDate.onSaveInstanceState().getInt(MONTH);
            int day = startDate.onSaveInstanceState().getInt(DAY);
            startCalendar.set(year,month,day);

        } else if (startTime.onSaveInstanceState() != null) {

            int hour = startTime.onSaveInstanceState().getInt(HOUR);
            int minute = startTime.onSaveInstanceState().getInt(MINUTE);
            startCalendar.set(Calendar.HOUR_OF_DAY,hour);
            startCalendar.set(Calendar.MINUTE,minute);

        }
        if (endDate.onSaveInstanceState() != null) {

            int year = startDate.onSaveInstanceState().getInt(YEAR);
            int month= startDate.onSaveInstanceState().getInt(MONTH);
            int day = startDate.onSaveInstanceState().getInt(DAY);
            endCalender.set(year,month,day);

        } else if (endTime.onSaveInstanceState() != null) {

            int hour = startTime.onSaveInstanceState().getInt(HOUR);
            int minute = startTime.onSaveInstanceState().getInt(MINUTE);
            endCalender.set(Calendar.HOUR_OF_DAY, hour);
            endCalender.set(Calendar.MINUTE, minute);
        }

        String strStartTime = (String) DateFormat.format("yyyy-MM-dd hh:mm:ss",startCalendar);
        String strEndTime = (String) DateFormat.format("yyyy-MM-dd hh:mm:ss",endCalender);
//
//        if (strStartTime == null ) {
//            strStartTime = (String) DateFormat.format("yyyy-MM-dd hh:mm:ss",calendar.getTime());
//        }
//        if (strEndTime == null) {
//            strEndTime = (String) DateFormat.format("yyyy-MM-dd hh:mm:ss",addHour(1).getTime());
//        }


        String finalStrStartTime = strStartTime;
        String finalStrEndTime = strEndTime;
//        reserveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                updateDate(activity.session, finalStrStartTime, finalStrEndTime);
//
//
//            }
//        });



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
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

            String text = (String) DateFormat.format("yyyy年MM月dd日",calendar);
            textView.setText(text);

        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
            calendar.set(Calendar.MINUTE,minute);

            String  text = (String) DateFormat.format("kk時mm分",calendar);
            textView.setText(text);
        }
    }



    /**
     * 現在の日付・時刻から指定の【時間】を加算・減算した結果を返します。
     * @param addHour 加算・減算する時間
     * @return    計算後の Calendar インスタンス。
     */
    public static Calendar addHour(int addHour){
        return add(null,0,0,0,addHour,0,0,0);
    }
    /**
     * 各時間フィールドに設定する数量が0の場合は、現在の値が設定されます。
     * java.util.GregorianCalendarの内部処理では以下の分岐を行っている。
     *     if (amount == 0) {
     *         return;
     *     }
     *
     * @param cal 日付時刻の指定があればセットする。
     *     nullの場合、現在の日付時刻で新しいCalendarインスタンスを生成する。
     * @param addHour 加算・減算する時間
     */
    public static Calendar add(Calendar cal,
                               int addYera,int addMonth,int addDate,
                               int addHour,int addMinute,int addSecond,
                               int addMillisecond){
        if (cal == null) {
            cal = Calendar.getInstance();
        }
        cal.add(Calendar.HOUR_OF_DAY, addHour);
        return cal;
    }

     public void updateDate( Session session, String startTime, String endTime ) {

         ApiService service = Util.getService();
         HashMap<String, String> body = new HashMap<>();
         body.put("start_time",startTime);
         body.put("end_time",endTime);
         Observable<SessionDetail> token = service.updateSession(LoginUser.getToken(), session.id, body);
         cd.add(token.subscribeOn(Schedulers.io())
                 .observeOn(AndroidSchedulers.mainThread())
                 .unsubscribeOn(Schedulers.io())
                 .subscribe(
                         list -> {
                             Log.v("sessionTime", list.data.start_time);
                             Log.v("sessionTime", list.data.end_time);
                             if(activity != null){
                                 activity.session.start_time = list.data.start_time;
                                 activity.session.end_time = list.data.end_time;
                             }

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
