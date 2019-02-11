package com.hew.second.gathering.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridView;

import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.views.adapters.GroupMemberAdapter;
import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.views.adapters.MemberAdapter;
import com.hew.second.gathering.R;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.GroupDetail;
import com.hew.second.gathering.api.MemberInfo;
import com.hew.second.gathering.api.TokenInfo;
import com.hew.second.gathering.api.Util;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static android.app.Activity.RESULT_OK;
import static com.hew.second.gathering.activities.BaseActivity.SNACK_MESSAGE;


public class EditGroupFragment extends Fragment {
    private static final String MESSAGE = "message";
    int groupId = -1;

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

    public void removeFocus(){
        InputMethodManager inputMethodMgr = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        inputMethodMgr.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_edit_group,
                container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        activity.setTitle("グループ編集");

        FloatingActionButton fab = activity.findViewById(R.id.fab_addUserToGroup);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO 追加ユーザー一覧
                //Intent intent = new Intent(activity.getApplication(), EditGroupActivity.class);
                //startActivity(intent);
            }
        });
        EditText groupName = getActivity().findViewById(R.id.group_name);
        groupName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた場合キーボードを非表示にする
                InputMethodManager inputMethodMgr = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        Intent beforeIntent = activity.getIntent();
        groupId = beforeIntent.getIntExtra("GROUP_ID", -1);//設定したkeyで取り出す
        ApiService service = Util.getService();
        Observable<TokenInfo> token = service.getRefreshToken(LoginUser.getToken());
        Util.setLoading(true, activity);
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    return service.getGroupDetail(LoginUser.getToken(), groupId);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Util.setLoading(false, getActivity());
                            updateList(list.data);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            Util.setLoading(false, getActivity());
                            Intent intent = new Intent();
                            intent.putExtra(SNACK_MESSAGE, "グループ情報の取得に失敗しました。");
                            getActivity().setResult(RESULT_OK, intent);
                            getActivity().finish();
                        }
                );
    }

    private void updateList(GroupDetail.GroupDetailInfo gdi) {
        // ListView生成
        GridView gridView = getActivity().findViewById(R.id.gridView_group);
        EditText groupName = getActivity().findViewById(R.id.group_name);
        groupName.setText(gdi.name);
        ArrayList<MemberAdapter.Data> ar = new ArrayList<>();

        for (MemberInfo m : gdi.users) {
            ar.add(new MemberAdapter.Data(m.id, m.unique_id, m.username));
        }
        GroupMemberAdapter adapter = new GroupMemberAdapter(ar);

        // ListViewにadapterをセット
        gridView.setAdapter(adapter);
    }

    public void saveGroupName() {
        ApiService service = Util.getService();
        EditText groupName = getActivity().findViewById(R.id.group_name);
        HashMap<String, String> body = new HashMap<>();
        body.put("name", groupName.getText().toString());
        Observable<TokenInfo> token = service.getRefreshToken(LoginUser.getToken());
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    return service.updateGroupName(LoginUser.getToken(), groupId, body);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Util.setLoading(false, getActivity());
                            Intent intent = new Intent();
                            intent.putExtra(SNACK_MESSAGE, "グループ名を更新しました。");
                            getActivity().setResult(RESULT_OK, intent);
                            getActivity().finish();
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            Util.setLoading(false, getActivity());
                            Intent intent = new Intent();
                            if (throwable instanceof HttpException && ((HttpException) throwable).code() == 409) {
                                intent.putExtra(SNACK_MESSAGE, "グループ名の変更はありません。");
                            } else {
                                intent.putExtra(SNACK_MESSAGE, "グループ名の更新に失敗しました。");
                            }
                            getActivity().setResult(RESULT_OK, intent);
                            getActivity().finish();
                        }
                );
    }
}
