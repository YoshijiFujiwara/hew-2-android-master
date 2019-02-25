package com.hew.second.gathering.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Session;

import java.text.SimpleDateFormat;
import java.util.Calendar;

//　開始時刻設定
public class StartTimeFragment extends SessionBaseFragment {

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

        Calendar calendar = Calendar.getInstance();
//      現在 年 月 日 時 分
        int year = calendar.get(Calendar.YEAR);
        int monthOfYear = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TextView startDateText = getActivity().findViewById(R.id.start_date);
        TextView startTimeText = getActivity().findViewById(R.id.start_timer);
        TextView endDateText = getActivity().findViewById(R.id.end_date);
        TextView endTimeText = getActivity().findViewById(R.id.end_timer);

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy年MM月dd日E曜日");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH時mm分");

//        開始　終了設定されないとき(終了時間は初期値は一時間加算)
        startDateText.setText(sdfDate.format(calendar.getTime()));
        startTimeText.setText(sdfTime.format(calendar.getTime()));
        endDateText.setText(sdfDate.format(calendar.getTime()));
        endTimeText.setText(sdfTime.format(addHour(1).getTime()));

        if (savedInstanceState == null) {
//            第1 context 第2 日付が選択された時のコールバック 第3 年の値 第4 月の値 第5 日の値
            DatePickerDialog startDate = new DatePickerDialog(getActivity(),new DateSetHandler(startDateText),year,monthOfYear,dayOfMonth);
            TimePickerDialog startTime = new TimePickerDialog(getActivity(),new DateSetHandler(startTimeText),hourOfDay,minute,true);
            DatePickerDialog endDate = new DatePickerDialog(getActivity(),new DateSetHandler(endDateText),year,monthOfYear,dayOfMonth);
            TimePickerDialog endTime = new TimePickerDialog(getActivity(),new DateSetHandler(endTimeText),hourOfDay,minute,true);

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
        }
    }
//      日付・時刻選択された時TextViewに格納
    private class DateSetHandler implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

        private TextView textView;
        private Calendar calendar = Calendar.getInstance();
        java.text.DateFormat dfFull = java.text.DateFormat.getDateInstance(java.text.DateFormat.FULL);
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

//          値格納予定
            CharSequence text = DateFormat.format("yyyy年MM月dd日E曜日",calendar);
            textView.setText(text);

        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
            calendar.set(Calendar.MINUTE,minute);

            CharSequence text = DateFormat.format("kk時mm分",calendar);
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

    public void headerTextSet(Session data) {
//        if (getActivity() != null) {
//            TextView locationText = getActivity().findViewById(R.id.stf_location);
//            TextView dateText = getActivity().findViewById(R.id.stf_date);
//            TextView numberText = getActivity().findViewById(R.id.stf_number);
//
//
//            locationText.setText(data.shop_id);
//            dateText.setText();
        }
}
