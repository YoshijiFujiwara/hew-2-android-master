package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static com.hew.second.gathering.activities.BaseActivity.INTENT_EDIT_GROUP;

public class GroupFragment extends Fragment {
    ArrayList<GroupAdapter.Data> ar = new ArrayList<GroupAdapter.Data>();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    GroupAdapter adapter = null;

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

    public void removeFocus() {
        SearchView searchView = getActivity().findViewById(R.id.searchView);
        searchView.clearFocus();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        activity.setTitle("グループ一覧");

        FloatingActionButton fab = activity.findViewById(R.id.fab_newGroup);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroup();
            }
        });

        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        GridView gridView = activity.findViewById(R.id.gridView_group);

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

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            switch (view.getId()) {
                case R.id.delete_group:
                    ApiService service = Util.getService();
                    Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
                    Util.setLoading(true, getActivity());
                    token.subscribeOn(Schedulers.io())
                            .flatMapCompletable(result -> {
                                LoginUser.setToken(result.access_token);
                                return service.deleteGroup(LoginUser.getToken(), adapter.getList().get(position).id);
                            })
                            .observeOn(AndroidSchedulers.mainThread())
                            .unsubscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> {
                                        fetchList();
                                        final Snackbar snackbar = Snackbar.make(getView(), "グループを削除しました", Snackbar.LENGTH_LONG);
                                        snackbar.getView().setBackgroundColor(Color.BLACK);
                                        snackbar.setActionTextColor(Color.WHITE);
                                        snackbar.show();
                                    }, // 終了時
                                    (throwable) -> {
                                        Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                                        Util.setLoading(false, getActivity());
                                        if (throwable instanceof HttpException && ((HttpException) throwable).code() == 409){
                                            //JSONObject jObjError = new JSONObject(((HttpException)throwable).response().errorBody().string());
                                            final Snackbar snackbar = Snackbar.make(getView(),  "このグループを使用しているデフォルト設定があるので、削除できません",  Snackbar.LENGTH_LONG);
                                            snackbar.getView().setBackgroundColor(Color.BLACK);
                                            snackbar.setActionTextColor(Color.WHITE);
                                            snackbar.show();
                                        }
                                    }
                            );
                    break;
                default:
                    // メンバ編集画面へグループIDを渡す
                    Intent intent = new Intent(activity.getApplication(), EditGroupActivity.class);
                    intent.putExtra("GROUP_ID", ar.get(position).id);
                    startActivityForResult(intent, INTENT_EDIT_GROUP);
            }});
    }

    @Override
    public void onResume() {
        super.onResume();
        Util.setLoading(true, getActivity());
        fetchList();
    }

    private void createGroup(){
        Util.setLoading(true, getActivity());
        ApiService service = Util.getService();
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        HashMap<String, String> body = new HashMap<>();
        // TODO:デフォルトグループ名
        body.put("name", "グループ");
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    return service.createGroup(LoginUser.getToken(),body);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Util.setLoading(false, getActivity());
                            Intent intent = new Intent(getActivity().getApplication(), EditGroupActivity.class);
                            intent.putExtra("GROUP_ID", list.data.id);
                            startActivityForResult(intent, INTENT_EDIT_GROUP);
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

    private void fetchList() {
        ApiService service = Util.getService();
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    return service.getGroupList(LoginUser.getToken());
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Util.setLoading(false, getActivity());
                            mSwipeRefreshLayout.setRefreshing(false);
                            updateList(list.data);
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

    private void updateList(List<Group> data) {
        // ListView生成
        GridView gridView = getActivity().findViewById(R.id.gridView_group);
        ar.clear();
        for (Group m : data) {
            ar.add(new GroupAdapter.Data(m.id, m.name, m.users.size() + "名"));
        }
        adapter = new GroupAdapter(ar);
        // ListViewにadapterをセット
        gridView.setAdapter(adapter);
    }

}
