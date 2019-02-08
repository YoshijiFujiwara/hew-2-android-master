package com.example.eventer.member;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.eventer.member.api.ApiService;
import com.example.eventer.member.api.MemberInfo;
import com.example.eventer.member.api.TokenInfo;
import com.example.eventer.member.api.Util;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class MemberFragment extends Fragment {
    Retrofit retrofit;
    private static final String MESSAGE = "message";

    public static MemberFragment newInstance(String message) {
        MemberFragment fragment = new MemberFragment();

        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    public static MemberFragment newInstance() {
        MemberFragment fragment = new MemberFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_member,
                container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        activity.setTitle( "メンバー一覧" );
        retrofit = Util.getRetrofit();

        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity.getApplication(), InputMemberActivity.class);
                startActivity(intent);
            }
        });

        ApiService service = retrofit.create(ApiService.class);
        Observable<TokenInfo> token = service.getRefreshToken(Util.getToken());

        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    Util.setToken(result.access_token);
                    return service.getMemberList(Util.getToken());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> updateList(list.data),  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            // ログインアクティビティへ遷移
                            Intent intent = new Intent(getActivity().getApplication(), LoginActivity.class);
                            startActivity(intent);
                        }
                );
    }

    private void updateList(List<MemberInfo> data) {
        // ListView生成
        ListView listView = getActivity().findViewById(R.id.member_list);
        ArrayList<String> ar = new ArrayList<String>();

        for (MemberInfo m : data) {
            ar.add(m.name);
        }
        String list[] = ar.toArray(new String[0]);
        MemberAdapter adapter = new MemberAdapter(list);
        // ListViewにadapterをセット
        listView.setAdapter(adapter);
    }

}
