package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;

import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.activities.AddGroupMemberActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.Group;
import com.hew.second.gathering.api.GroupDetail;
import com.hew.second.gathering.api.GroupUser;
import com.hew.second.gathering.views.adapters.GroupMemberAdapter;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static android.app.Activity.RESULT_OK;
import static com.hew.second.gathering.activities.BaseActivity.INTENT_ADD_GROUP_MEMBER;
import static com.hew.second.gathering.activities.BaseActivity.INTENT_EDIT_GROUP;
import static com.hew.second.gathering.activities.BaseActivity.SNACK_MESSAGE;


public class EditGroupFragment extends BaseFragment {
    private static final String MESSAGE = "message";
    int groupId = -1;
    GroupMemberAdapter adapter = null;

    public static EditGroupFragment newInstance() {
        return new EditGroupFragment();
    }

    public static EditGroupFragment newInstance(String message) {
        EditGroupFragment fragment = new EditGroupFragment();

        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    public void removeFocus() {
        InputMethodManager inputMethodMgr = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
        inputMethodMgr.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_group,
                container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.setTitle("グループ編集");

        Intent beforeIntent = activity.getIntent();
        groupId = beforeIntent.getIntExtra("GROUP_ID", -1);//設定したkeyで取り出す

        FloatingActionButton fab = activity.findViewById(R.id.fab_addUserToGroup);
        fab.setOnClickListener((v) -> {
            Intent intent = new Intent(activity.getApplication(), AddGroupMemberActivity.class);
            intent.putExtra("GROUP_ID", groupId);
            startActivity(intent);
        });
        EditText groupName = activity.findViewById(R.id.group_name);
        groupName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた場合キーボードを非表示にする
                InputMethodManager inputMethodMgr = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        GridView gridView = activity.findViewById(R.id.gridView_group);
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            switch (view.getId()) {
                case R.id.delete_group_member:
                    ApiService service = Util.getService();
                    Completable token = service.deleteGroupUser(LoginUser.getToken(), groupId, adapter.getList().get(position).id);
                    cd.add(token.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .unsubscribeOn(Schedulers.io())
                            .subscribe(
                                    () -> {
                                        if (activity != null) {
                                            fetchList();
                                            final Snackbar snackbar = Snackbar.make(activity.findViewById(R.id.content), "メンバーを削除しました", Snackbar.LENGTH_LONG);
                                            snackbar.getView().setBackgroundColor(Color.BLACK);
                                            snackbar.setActionTextColor(Color.WHITE);
                                            snackbar.show();
                                        }
                                    }, // 終了時
                                    (throwable) -> {
                                        Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                                        if (activity != null && !cd.isDisposed() && throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                            Intent intent = new Intent(activity.getApplication(), LoginActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                            ));
                    break;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        fetchList();
    }

    private void fetchList() {
        ApiService service = Util.getService();
        Observable<GroupDetail> token = service.getGroupDetail(LoginUser.getToken(), groupId);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (activity != null) {
                                updateList(list.data);
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                activity.finish();
                            }
                        }
                ));
    }

    private void updateList(Group gdi) {
        // ListView生成
        GridView gridView = activity.findViewById(R.id.gridView_group);
        EditText groupName = activity.findViewById(R.id.group_name);
        groupName.setText(gdi.name);
        ArrayList<GroupUser> ar = new ArrayList<>(gdi.users);
        adapter = new GroupMemberAdapter(ar);

        if(gridView != null){
            // ListViewにadapterをセット
            gridView.setAdapter(adapter);
        }
    }

    public void saveGroupName() {
        ApiService service = Util.getService();
        EditText groupName = activity.findViewById(R.id.group_name);
        HashMap<String, String> body = new HashMap<>();
        body.put("name", groupName.getText().toString());
        Observable<GroupDetail> token = service.updateGroupName(LoginUser.getToken(), groupId, body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (activity != null) {
                                Intent intent = new Intent();
                                intent.putExtra(SNACK_MESSAGE, "グループ名を更新しました。");
                                activity.setResult(RESULT_OK, intent);
                                activity.finish();
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                Intent intent = new Intent();
                                if (throwable instanceof HttpException && ((HttpException) throwable).code() == 409) {
                                    intent.putExtra(SNACK_MESSAGE, "グループ名の変更はありません。");
                                } else if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                    Intent intent2 = new Intent(activity.getApplication(), LoginActivity.class);
                                    startActivity(intent2);
                                }
                                activity.setResult(RESULT_OK, intent);
                                activity.finish();
                            }
                        }
                ));
    }
}
