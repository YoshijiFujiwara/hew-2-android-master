package com.example.eventer.member;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

import com.example.eventer.member.api.ApiService;
import com.example.eventer.member.api.GroupInfo;
import com.example.eventer.member.api.MemberInfo;
import com.example.eventer.member.api.TokenInfo;
import com.example.eventer.member.api.Util;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class GroupFragment extends Fragment {
    Retrofit retrofit;

    public static GroupFragment newInstance() {
        GroupFragment fragment = new GroupFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_group,
                container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        activity.setTitle( "グループ一覧" );
        retrofit = Util.getRetrofit();

        FloatingActionButton fab = activity.findViewById(R.id.fab_newGroup);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
                //Intent intent = new Intent(activity.getApplication(), InputGroupActivity.class);
                //startActivity(intent);
            }
        });

        ApiService service = retrofit.create(ApiService.class);
        Observable<TokenInfo> token = service.getRefreshToken(Util.getToken());
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    Util.setToken(result.access_token);
                    return service.getGroupList(Util.getToken());
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

    private void updateList(List<GroupInfo> data) {
        // ListView生成
        GridView gridView = getActivity().findViewById(R.id.gridView_group);
        ArrayList<GroupAdapter.Data> ar = new ArrayList<GroupAdapter.Data>();

        for (GroupInfo m : data) {
            ar.add(new GroupAdapter.Data(m.id,m.name,m.users.size()+"名"));
        }
        GroupAdapter adapter = new GroupAdapter(ar);

        // ListViewにadapterをセット
        gridView.setAdapter(adapter);
    }

}
