package com.hew.second.gathering.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hew.second.gathering.R;

public class SessionMainFragment extends Fragment {

    TextView session_name_tv, session_people_tv, session_time_tv;
    Button session_finish_btn;

    public static SessionMainFragment newInstance() {
        return new SessionMainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_finish, container, false);

        // TextViewにアイコンをつける
        session_name_tv = (TextView) view.findViewById(R.id.session_name);
        session_people_tv = (TextView) view.findViewById(R.id.session_people);
        session_time_tv = (TextView) view.findViewById(R.id.session_time);

        session_name_tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_location_on, 0, 0, 0);
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
                ad.setMessage("セッション名\nセッション日時\nセッション参加人数");
                ad.setButton("イベント終了", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // todo セッションの削除apiを叩く
                        dialog.dismiss();
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
        return view;
    }
}
