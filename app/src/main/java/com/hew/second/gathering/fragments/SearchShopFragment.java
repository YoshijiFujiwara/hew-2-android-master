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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.SearchArgs;
import com.hew.second.gathering.activities.EditGroupActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.FriendList;
import com.hew.second.gathering.api.Group;
import com.hew.second.gathering.api.GroupDetail;
import com.hew.second.gathering.api.GroupList;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.hotpepper.Genre;
import com.hew.second.gathering.hotpepper.GenreList;
import com.hew.second.gathering.hotpepper.GenreResult;
import com.hew.second.gathering.hotpepper.GourmetResult;
import com.hew.second.gathering.hotpepper.HpHttp;
import com.hew.second.gathering.views.adapters.GroupAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static com.hew.second.gathering.activities.BaseActivity.INTENT_EDIT_GROUP;

public class SearchShopFragment extends SessionBaseFragment {
    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private List<Genre> genreList = new ArrayList<>();
    private Spinner spinner = null;

    public static SearchShopFragment newInstance() {
        return new SearchShopFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_search_shop,
                container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SlidingUpPanelLayout sup = activity.findViewById(R.id.sliding_layout_search);
        sup.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);

        spinner = activity.findViewById(R.id.spinner_genre);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                String item = (String) spinner.getSelectedItem();
                String result = null;
                for (Genre g : genreList) {
                    if (g.name.equals(item)) {
                        result = g.code;
                        break;
                    }
                }
                SearchArgs.genre = result;
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

        Spinner rangeSpinner = activity.findViewById(R.id.spinner_range);
        rangeSpinner.setSelection(2);
        rangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                String item = (String) spinner.getSelectedItem();
                Integer result = 3;
                for (int i = 0; i < SearchArgs.rangeList.size(); i++) {
                    if (SearchArgs.rangeList.get(i).toString().equals(item)) {
                        result = i;
                        break;
                    }
                }
                SearchArgs.range = result;
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

        SearchView searchView = activity.findViewById(R.id.searchView_keyword);
        searchView.setOnClickListener((v) -> {
            searchView.setIconified(false);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                SearchArgs.keyword = s;
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                SearchArgs.keyword = s;
                return true;
            }
        });


        HashMap<String, String> body = new HashMap<>();
        Observable<GenreResult> GenreResult = HpHttp.getService().getGenreList();
        cd.add(GenreResult.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (activity != null) {
                                genreList = new ArrayList<>(list.results.genre);
                                ArrayList<String> data = new ArrayList<>();
                                data.add("（指定なし）");
                                for (Genre g : genreList) {
                                    data.add(g.name);
                                }
                                spinner = activity.findViewById(R.id.spinner_genre);
                                ArrayAdapter adapter =
                                        new ArrayAdapter(activity, android.R.layout.simple_spinner_item, data.toArray(new String[0]));
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    // ログインアクティビティへ遷移
                                    Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent);
                                }
                            }
                        }
                ));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
