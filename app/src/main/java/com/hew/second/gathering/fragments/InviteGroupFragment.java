package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.Group;
import com.hew.second.gathering.api.GroupList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.GroupAdapter;
import com.hew.second.gathering.views.adapters.SessionAddMemberAdapter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class InviteGroupFragment extends SessionBaseFragment {


    private SwipeRefreshLayout mSwipeRefreshLayout;
    private GroupAdapter adapter = null;
    private ArrayList<Group> ar = new ArrayList<>();
    private GridView gridView = null;

    public static InviteGroupFragment newInstance() {
        return new InviteGroupFragment();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_invite_group, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSwipeRefreshLayout = activity.findViewById(R.id.swipeLayout_invite_group);
        // 色設定
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccentDark);
        // Listenerをセット
        mSwipeRefreshLayout.setOnRefreshListener(() -> fetchList());

        gridView = activity.findViewById(R.id.gridView_group);
        gridView.setOnItemClickListener((parent, view, position, id) -> {

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchList();
    }


    private void fetchList() {
        ApiService service = Util.getService();
        Observable<GroupList> token = service.getGroupList(LoginUser.getToken());
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (activity != null) {
                                mSwipeRefreshLayout.setRefreshing(false);
                                updateList(list.data);
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed() && throwable instanceof HttpException && ((HttpException) throwable).code() == 401) {
                                Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                ));
    }

    private void updateList(List<Group> data) {
        // ListView生成
        GridView gridView = activity.findViewById(R.id.gridView_group);
        ar.clear();
        ar.addAll(data);
        adapter = new GroupAdapter(ar);
        // ListViewにadapterをセット
        gridView.setAdapter(adapter);
    }
}
