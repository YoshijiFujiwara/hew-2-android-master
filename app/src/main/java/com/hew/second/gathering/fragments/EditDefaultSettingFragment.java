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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.AddGroupMemberActivity;
import com.hew.second.gathering.activities.EditDefaultSettingActivity;
import com.hew.second.gathering.activities.EditGroupActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.activities.MainActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.DefaultSetting;
import com.hew.second.gathering.api.DefaultSettingDetail;
import com.hew.second.gathering.api.Group;
import com.hew.second.gathering.api.GroupDetail;
import com.hew.second.gathering.api.GroupList;
import com.hew.second.gathering.api.GroupUser;
import com.hew.second.gathering.api.JWT;
import com.hew.second.gathering.api.Util;
import com.hew.second.gathering.views.adapters.GroupAdapter;
import com.hew.second.gathering.views.adapters.GroupMemberAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static android.app.Activity.RESULT_OK;
import static com.hew.second.gathering.activities.BaseActivity.INTENT_ADD_GROUP_MEMBER;
import static com.hew.second.gathering.activities.BaseActivity.INTENT_EDIT_DEFAULT;
import static com.hew.second.gathering.activities.BaseActivity.INTENT_EDIT_GROUP;
import static com.hew.second.gathering.activities.BaseActivity.SNACK_MESSAGE;


public class EditDefaultSettingFragment extends BaseFragment {
    private static final String MESSAGE = "message";
    int defaultSettingId = -1;
    ArrayList<GroupAdapter.Data> ar = new ArrayList<GroupAdapter.Data>();
    GroupAdapter adapter = null;
    private List<Group> groupList = new ArrayList<>();
    private Spinner spinner = null;

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
        InputMethodManager inputMethodMgr = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
        inputMethodMgr.hideSoftInputFromWindow(getView().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_default,
                container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.setTitle("デフォルト編集");


        Intent beforeIntent = activity.getIntent();
        defaultSettingId = beforeIntent.getIntExtra("DEFAULTSETTING_ID", -1);//設定したkeyで取り出す

        EditText defaultName = activity.findViewById(R.id.default_input);
        defaultName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた場合キーボードを非表示にする
                InputMethodManager inputMethodMgr = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        EditText startTime = activity.findViewById(R.id.start_time);
        startTime.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた場合キーボードを非表示にする
                InputMethodManager inputMethodMgr = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
//        RadioGroup mRadioGroup = getActivity().findViewById(R.id.RadioGroup);
//        mRadioGroup.setOnCheckedChangeListener(this);
//
//        // 選択されているRadioButonのIDを取得する
//        // どれも選択されていなければgetCheckedRadioButtonIdは-1が返ってくる
//        int checkedId = mRadioGroup.getCheckedRadioButtonId();

        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        Observable<GroupList> token = service.getGroupList(LoginUser.getToken());
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (activity != null) {
                                groupList = new ArrayList<>(list.data);
                                ArrayList<String> data = new ArrayList<>();
                                for (Group g : groupList) {
                                    data.add(g.name);
                                }
                                spinner = activity.findViewById(R.id.group_spinner);
                                ArrayAdapter adapter =
                                        new ArrayAdapter(activity, android.R.layout.simple_spinner_item, data.toArray(new String[0]));
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);
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


        Button saveButton = activity.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // データ保存
               Util.setLoading(true,activity);
               saveDefaultSettingName();
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
        Observable<DefaultSettingDetail> token = service.getDefaultSettingDetail(LoginUser.getToken(), defaultSettingId);
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

    private void updateList(DefaultSetting gdi) {
        EditText defaultName = activity.findViewById(R.id.default_input);
        EditText startTime = activity.findViewById(R.id.start_time);
        Spinner spinner = activity.findViewById(R.id.group_spinner);
        RadioGroup mRadioGroup = activity.findViewById(R.id.RadioGroup);
//        mRadioGroup.setOnCheckedChangeListener((RadioGroup.OnCheckedChangeListener) this);

        // 選択されているRadioButonのIDを取得する
        // どれも選択されていなければgetCheckedRadioButtonIdは-1が返ってくる
//        int checkedId = mRadioGroup.getCheckedRadioButtonId();

        defaultName.setText(gdi.name);
        startTime.setText(gdi.timer);
        spinner.setSelection(gdi.group.id);
//        mRadioGroup.check(R.id.specific_location);
    }

    public void saveDefaultSettingName() {
        ApiService service = Util.getService();

        EditText defaultName = activity.findViewById(R.id.default_input);
        EditText startTime = activity.findViewById(R.id.start_time);
        Spinner spinner = activity.findViewById(R.id.group_spinner);
        RadioGroup mRadioGroup = activity.findViewById(R.id.RadioGroup);

        HashMap<String, String> body = new HashMap<>();

        body.put("name", defaultName.getText().toString());
        body.put("timer", startTime.getText().toString());
        body.put("group", spinner.getSelectedItem().toString());
//        body.put("", mRadioGroup.getCheckedRadioButtonId().toString());

        Observable<DefaultSettingDetail> token = service.updateDefaultSettingName(LoginUser.getToken(), defaultSettingId, body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (activity != null) {
                                Intent intent = new Intent();
                                intent.putExtra(SNACK_MESSAGE, "デフォルトを更新しました。");
                                activity.setResult(RESULT_OK, intent);
                                activity.finish();
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                Intent intent = new Intent();
                                if (throwable instanceof HttpException && ((HttpException) throwable).code() == 409) {
                                    intent.putExtra(SNACK_MESSAGE, "デフォルトの変更はありません。");
                                } else {
                                    intent.putExtra(SNACK_MESSAGE, "デフォルトの更新に失敗しました。");
                                }
                                activity.setResult(RESULT_OK, intent);
                                activity.finish();
                            }
                        }
                ));
    }
}
