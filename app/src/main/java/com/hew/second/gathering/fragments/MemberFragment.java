package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.activities.AddMemberActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.views.adapters.MemberAdapter;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MemberFragment extends Fragment {
    private static final String MESSAGE = "message";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MemberAdapter adapter = null;
    private ArrayList<Friend> ar = new ArrayList<>();
    private ListView listView = null;

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


    public void removeFocus() {
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
            Intent intent = new Intent(activity.getApplication(), AddMemberActivity.class);
            startActivity(intent);
        });
        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        SearchView searchView = activity.findViewById(R.id.searchView);
        searchView.setOnClickListener((v) -> {
            searchView.setIconified(false);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                List<Friend> filteredItems;
                // フィルター処理
                if (s.isEmpty()) {
                    filteredItems = new ArrayList<>(ar);
                } else {
                    filteredItems = new ArrayList<>();
                    for (Friend item : ar) {
                        if (item.unique_id.toLowerCase().contains(s.toLowerCase()) || item.username.toLowerCase().contains(s.toLowerCase())) { // テキストがqueryを含めば検索にHITさせる
                            filteredItems.add(item);
                        }
                    }
                }
                // adapterの更新処理
                adapter.clear();
                adapter.addAll(filteredItems);
                adapter.notifyDataSetChanged();
                return true;
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
    }

    @Override
    public void onResume() {
        super.onResume();
        Util.setLoading(true, getActivity());
        fetchList();
    }

    private void fetchList() {
        ApiService service = Util.getService();
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    return service.getFriendList(LoginUser.getToken());
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

    private void updateList(List<Friend> data) {
        // ListView生成
        listView = getActivity().findViewById(R.id.member_list);
        ArrayList<Friend> list = new ArrayList<>(data);
        ar = new ArrayList<>(data);
        adapter = new MemberAdapter(list);
        // ListViewにadapterをセット
        listView.setAdapter(adapter);
    }

}
