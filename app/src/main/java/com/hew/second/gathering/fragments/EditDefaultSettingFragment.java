package com.hew.second.gathering.fragments;

import android.app.Activity;
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

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.AddGroupMemberActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.DefaultSetting;
import com.hew.second.gathering.api.Group;
import com.hew.second.gathering.api.GroupUser;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.GroupMemberAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static android.app.Activity.RESULT_OK;
import static com.hew.second.gathering.activities.BaseActivity.INTENT_ADD_GROUP_MEMBER;
import static com.hew.second.gathering.activities.BaseActivity.SNACK_MESSAGE;


public class EditDefaultSettingFragment extends Fragment {
    private static final String MESSAGE = "message";
    int defaultSettingId = -1;
    private CompositeDisposable cd = new CompositeDisposable();

    public static EditDefaultSettingFragment newInstance() {
        return new EditDefaultSettingFragment();
    }

    public static EditDefaultSettingFragment newInstance(String message) {
        EditDefaultSettingFragment fragment = new EditDefaultSettingFragment();

        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        fragment.setArguments(args);
        return fragment;
    }

    public void removeFocus() {
        InputMethodManager inputMethodMgr = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        inputMethodMgr.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onDestroy(){
        cd.clear();
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_edit_default,
                container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Activity activity = getActivity();
        activity.setTitle("デフォルト編集");

        Intent beforeIntent = activity.getIntent();
        defaultSettingId = beforeIntent.getIntExtra("DEFAULTSETTING_ID", -1);//設定したkeyで取り出す

        EditText defaultName = getActivity().findViewById(R.id.default_input);
        defaultName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた場合キーボードを非表示にする
                InputMethodManager inputMethodMgr = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        EditText startTime = getActivity().findViewById(R.id.start_time);
        startTime.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた場合キーボードを非表示にする
                InputMethodManager inputMethodMgr = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();
        Util.setLoading(true, getActivity());
        fetchList();
    }

    private void fetchList(){
        ApiService service = Util.getService();
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    return service.getDefaultSettingDetail(LoginUser.getToken(), defaultSettingId);
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
                            intent.putExtra(SNACK_MESSAGE, "デフォルト情報の取得に失敗しました。");
                            getActivity().setResult(RESULT_OK, intent);
                            getActivity().finish();
                        }
                );
    }

    private void updateList(DefaultSetting gdi) {

        EditText defaultName = getActivity().findViewById(R.id.default_input);
        EditText startTime = getActivity().findViewById(R.id.start_time);

        defaultName.setText(gdi.name);
        startTime.setText(gdi.timer);
    }

    public void saveDefaultSettingName() {
        ApiService service = Util.getService();
        EditText defaultName = getActivity().findViewById(R.id.default_input);
        EditText startTime = getActivity().findViewById(R.id.start_time);
        HashMap<String, String> body = new HashMap<>();
        body.put("name", defaultName.getText().toString());
        body.put("timer", startTime.getText().toString());
        Observable<JWT> token = service.getRefreshToken(LoginUser.getToken());
        token.subscribeOn(Schedulers.io())
                .flatMap(result -> {
                    LoginUser.setToken(result.access_token);
                    return service.updateDefaultSettingName(LoginUser.getToken(), defaultSettingId, body);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            Util.setLoading(false, getActivity());
                            Intent intent = new Intent();
                            intent.putExtra(SNACK_MESSAGE, "デフォルトを更新しました。");
                            getActivity().setResult(RESULT_OK, intent);
                            getActivity().finish();
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            Util.setLoading(false, getActivity());
                            Intent intent = new Intent();
                            if (throwable instanceof HttpException && ((HttpException) throwable).code() == 409) {
                                intent.putExtra(SNACK_MESSAGE, "デフォルトの変更はありません。");
                            } else {
                                intent.putExtra(SNACK_MESSAGE, "デフォルトの更新に失敗しました。");
                            }
                            getActivity().setResult(RESULT_OK, intent);
                            getActivity().finish();
                        }
                );
    }
}
