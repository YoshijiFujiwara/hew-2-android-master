package com.hew.second.gathering.fragments;

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
import android.widget.AdapterView;
import android.widget.GridView;

import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.views.adapters.GroupAdapter;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.EditGroupActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Group;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.hew.second.gathering.activities.BaseActivity.INTENT_EDIT_GROUP;

public class GroupFragment extends Fragment {
    ArrayList<GroupAdapter.Data> ar = new ArrayList<GroupAdapter.Data>();

    public static GroupFragment newInstance() {
        return new GroupFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_group,
                container, false);
    }

    public void removeFocus(){
        SearchView searchView = getActivity().findViewById(R.id.searchView);
        searchView.clearFocus();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        activity.setTitle( "グループ一覧" );

        FloatingActionButton fab = activity.findViewById(R.id.fab_newGroup);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
                //Intent intent = new Intent(activity.getApplication(), EditGroupActivity.class);
                //startActivity(intent);
            }
        });

        GridView gridView = activity.findViewById(R.id.gridView_group);
        // セルを選択されたら詳細画面フラグメント呼び出す
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                // メンバ編集画面へグループIDを渡す
                Intent intent = new Intent(activity.getApplication(), EditGroupActivity.class);
                intent.putExtra("GROUP_ID", ar.get(position).getId());
                startActivityForResult(intent,INTENT_EDIT_GROUP);
            }
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

        ApiService service = Util.getService();
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        Util.setLoading(true,activity);
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    return service.getGroupList(LoginUser.getToken());
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

    private void updateList(List<Group> data) {
        // ListView生成
        GridView gridView = getActivity().findViewById(R.id.gridView_group);
        ar.clear();

        for (Group m : data) {
            ar.add(new GroupAdapter.Data(m.id,m.name,m.users.size()+"名"));
        }
        GroupAdapter adapter = new GroupAdapter(ar);
        // ListViewにadapterをセット
        gridView.setAdapter(adapter);
    }

}
