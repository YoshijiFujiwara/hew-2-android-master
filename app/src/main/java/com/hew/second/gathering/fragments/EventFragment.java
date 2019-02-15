package com.hew.second.gathering.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.SelectedSession;
import com.hew.second.gathering.activities.EditGroupActivity;
import com.hew.second.gathering.activities.GurunaviMapsActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.hew.second.gathering.activities.BaseActivity.INTENT_EDIT_GROUP;
import static com.hew.second.gathering.api.Util.PREF_FILE_NAME;


public class EventFragment extends Fragment {


    public static EventFragment newInstance() {
        return new EventFragment();
    }

    private static class ViewHolder {
        ImageView shop_image;
        TextView title;
        TextView shop_name;
        TextView time;
        TextView count_member;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FloatingActionButton fab = getActivity().findViewById(R.id.fab_newSession);
        fab.setOnClickListener((v) -> createSession());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // ListViewに表示するデータ
        final ArrayList<String> items = new ArrayList<>();
        items.add("新入生歓迎会");
        items.add("同窓会");
        items.add("女子会");

        // ListViewをセット
        final ArrayAdapter adapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_list_item_1, items);
        ListView listView = (ListView) view.findViewById(R.id.listView_event);
        listView.setAdapter(adapter);

    }

    private void createSession(){
        ApiService service = Util.getService();
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        HashMap<String, String> body = new HashMap<>();
        body.put("name", "セッション");
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    return service.createSession(LoginUser.getToken(),body);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Util.setLoading(false, getActivity());
                            SelectedSession.setSessionId(getActivity().getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE),list.data.id);
                            Intent intent = new Intent(getActivity().getApplication(), GurunaviMapsActivity.class);
                            startActivity(intent);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            Util.setLoading(false, getActivity());
                            // ログインアクティビティへ遷移
                            Intent intent = new Intent(getActivity().getApplication(), LoginActivity.class);
                            startActivity(intent);
                        }
                );

    }
}
