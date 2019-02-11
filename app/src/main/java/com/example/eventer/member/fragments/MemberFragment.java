package com.example.eventer.member.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.eventer.member.LogUtil;
import com.example.eventer.member.views.adapters.MemberAdapter;
import com.example.eventer.member.R;
import com.example.eventer.member.activities.InputMemberActivity;
import com.example.eventer.member.activities.LoginActivity;
import com.example.eventer.member.api.ApiService;
import com.example.eventer.member.api.MemberInfo;
import com.example.eventer.member.api.TokenInfo;
import com.example.eventer.member.api.Util;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MemberFragment extends Fragment{
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

        View view = inflater.inflate(R.layout.fragment_member, container, false);
        return view;
    }


    public void removeFocus(){
        SearchView searchView = getActivity().findViewById(R.id.searchView);
        searchView.clearFocus();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        activity.setTitle("メンバー一覧");

        FloatingActionButton fab = activity.findViewById(R.id.fab);
        fab.setOnClickListener((v) -> {
            Intent intent = new Intent(activity.getApplication(), InputMemberActivity.class);
            startActivity(intent);
        });
        SearchView searchView = activity.findViewById(R.id.searchView);
        searchView.setOnClickListener((v) -> {
            searchView.setIconified(false);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // 送信
                // focusout
                searchView.clearFocus();
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                // テキスト変更
                return false;
            }
        });
        // フォーカスを失った時
        /*
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                searchView.clearFocus();
            }
        });
        */

        ApiService service = Util.getService();
        Observable<TokenInfo> token = service.getRefreshToken(Util.getToken());
        Util.setLoading(true,activity);
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    Util.setToken(result.access_token);
                    return service.getMemberList(Util.getToken());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Util.setLoading(false,getActivity());
                            updateList(list.data);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            Util.setLoading(false,getActivity());
                            // ログインアクティビティへ遷移
                            Intent intent = new Intent(getActivity().getApplication(), LoginActivity.class);
                            startActivity(intent);
                        }
                );
    }

    private void updateList(List<MemberInfo> data) {
        // ListView生成
        ListView listView = getActivity().findViewById(R.id.member_list);
        ArrayList<MemberAdapter.Data> ar = new ArrayList<>();

        for (MemberInfo m : data) {
            ar.add(new MemberAdapter.Data(m.id, m.unique_id, m.username));
        }
        MemberAdapter adapter = new MemberAdapter(ar);
        // ListViewにadapterをセット
        listView.setAdapter(adapter);
    }

}
