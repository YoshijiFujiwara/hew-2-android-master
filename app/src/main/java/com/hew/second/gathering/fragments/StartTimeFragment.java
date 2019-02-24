package com.hew.second.gathering.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hew.second.gathering.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

//　開始時刻設定
public class StartTimeFragment extends SessionBaseFragment {

    Calendar calendar;
    int startYear;
    int startMonth;
    int startDay;
    int endYear;
    int endMonth;
    int getEndMonth;

    public static StartTimeFragment newInstance() {
        Bundle args = new Bundle();
        StartTimeFragment fragment = new StartTimeFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//


        view = inflater.inflate(R.layout.fragment_starttime, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TextView startDateText = getActivity().findViewById(R.id.start_date);
        TextView startTimeText = getActivity().findViewById(R.id.start_timer);
        TextView endDateText = getActivity().findViewById(R.id.end_date);
        TextView endTimeText = getActivity().findViewById(R.id.end_timer);

//       開始時刻セット・フォーマット
        Calendar cl = Calendar.getInstance();
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy年MM月dd日");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH時mm分");
//        開始日時時刻・終了時刻が設定されてない場合の初期値
        startDateText.setText(sdfDate.format(cl.getTime()));
        startTimeText.setText(sdfTime.format(cl.getTime()));
        endDateText.setText(sdfDate.format(cl.getTime()));
        endTimeText.setText(sdfTime.format(addHour(1).getTime()));

        if (savedInstanceState == null) {

            DatePickerDialogFragment datePicker = new DatePickerDialogFragment();
            TimePickerDialogFragment timePicker = new TimePickerDialogFragment();

            startDateText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    datePicker.show(getFragmentManager(), "startDatePicker");

                }
            });

            startTimeText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timePicker.show(getFragmentManager(),"startTimePicker");
                }
            });

            endDateText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    datePicker.show(getFragmentManager(),"endDatePicker");
                }
            });

            endTimeText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timePicker.show(getFragmentManager(),"endDatePicker");
                }
            });




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
     * @param addYear 加算・減算する年数
     * @param addMonth 加算・減算する月数
     * @param addDate 加算・減算する日数
     * @param addHour 加算・減算する時間
     * @param addMinute 加算・減算する分
     * @param addSecond 加算・減算する秒
     * @param addMillisecond 加算・減算するミリ秒
     * @return    計算後の Calendar インスタンス。
     */
    public static Calendar add(Calendar cal,
                               int addYear,int addMonth,int addDate,
                               int addHour,int addMinute,int addSecond,
                               int addMillisecond){
        if (cal == null) {
            cal = Calendar.getInstance();
        }
        cal.add(Calendar.YEAR, addYear);
        cal.add(Calendar.MONTH, addMonth);
        cal.add(Calendar.DATE, addDate);
        cal.add(Calendar.HOUR_OF_DAY, addHour);
        cal.add(Calendar.MINUTE, addMinute);
        cal.add(Calendar.SECOND, addSecond);
        cal.add(Calendar.MILLISECOND, addMillisecond);
        return cal;
    }
}
