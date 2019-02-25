package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.DefaultSetting;
import com.hew.second.gathering.api.DefaultSettingDetail;
import com.hew.second.gathering.api.Profile;
import com.hew.second.gathering.api.ProfileDetail;
import com.hew.second.gathering.api.Util;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static android.app.Activity.RESULT_OK;
import static com.hew.second.gathering.activities.BaseActivity.SNACK_MESSAGE;


public class EditProfileFragment extends BaseFragment {
    private static final String MESSAGE = "message";
    int defaultSettingId = -1;
//    private Switch unique_id_search_flag;

    public static EditProfileFragment newInstance() {
        return new EditProfileFragment();
    }

    public static EditProfileFragment newInstance(String message) {
        EditProfileFragment fragment = new EditProfileFragment();

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
        view = inflater.inflate(R.layout.fragment_edit_profile,
                container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.setTitle("アカウント設定");

//        Intent beforeIntent = activity.getIntent();
//        defaultSettingId = beforeIntent.getIntExtra("DEFAULTSETTING_ID", -1);//設定したkeyで取り出す

        EditText uniqueId = activity.findViewById(R.id.unique_id);
        uniqueId.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた場合キーボードを非表示にする
                InputMethodManager inputMethodMgr = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        EditText userName = activity.findViewById(R.id.user_name);
        userName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた場合キーボードを非表示にする
                InputMethodManager inputMethodMgr = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        EditText mailAddress = activity.findViewById(R.id.mail_address);
        mailAddress.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // フォーカスが外れた場合キーボードを非表示にする
                InputMethodManager inputMethodMgr = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
                inputMethodMgr.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
        Switch uniqueIdSearchFlag = activity.findViewById(R.id.unique_id_search_flag);
        Switch usernameSearchFlag = activity.findViewById(R.id.username_search_flag);

        Button saveButton = activity.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // データ保存Loadin
               saveProfile();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void fetchList() {
        ApiService service = Util.getService();
        Observable<ProfileDetail> profile = service.getProfile(LoginUser.getToken());
        cd.add(profile.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            profile(list.data);
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {
                                Intent intent = new Intent(activity, LoginActivity.class);
                                startActivity(intent);
                            }
                        }
                ));
    }

    private void profile(Profile data) {
        EditText uniqueId = activity.findViewById(R.id.unique_id);
        EditText userName = activity.findViewById(R.id.user_name);
        EditText mailAddress = activity.findViewById(R.id.mail_address);
        Switch uniqueIdSearchFlag = activity.findViewById(R.id.unique_id_search_flag);
        Switch usernameSearchFlag = activity.findViewById(R.id.username_search_flag);

        uniqueId.setText(data.unique_id);
        userName.setText(data.username);
        mailAddress.setText(data.email);

        if (data.unique_id_search_flag == 1) {
            uniqueIdSearchFlag.setChecked(true);
        }
        else {
            uniqueIdSearchFlag.setChecked(false);
        }
    }

    public void saveProfile() {
        ApiService service = Util.getService();

        EditText uniqueId = activity.findViewById(R.id.unique_id);
        EditText userName = activity.findViewById(R.id.user_name);
        EditText mailAddress = activity.findViewById(R.id.mail_address);
        Switch uniqueIdSearchFlag = activity.findViewById(R.id.unique_id_search_flag);
        Switch usernameSearchFlag = activity.findViewById(R.id.username_search_flag);
        String uniqueIdFlag = "1";
        String usernameFlag = "1";

//        uniqueIdSearchFlag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked == true) {
//
//                }
//            }
//        });
//        switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked = true) {
//                    // The toggle is enabled
//
//                } else {
//                    // The toggle is disabled
//
//                }

//        int checkedId = mRadioGroup.getCheckedRadioButtonId();
//        String flag = "1";
//        switch (checkedId) {
//            case R.id.current_location:
//                flag = "1";
//                break;
//            case R.id.specific_location:
//                flag = "0";
//                break;
//        }

        HashMap<String, String> body = new HashMap<>();

        body.put("unique_id", uniqueId.getText().toString());
        body.put("username", userName.getText().toString());
        body.put("email", mailAddress.getText().toString());
//        body.put("unique_id_search_flag", );
//        body.put("username_search_flag", );

        Observable<ProfileDetail> token = service.updateProfileUser(LoginUser.getToken(), body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (activity != null) {
                                Intent intent = new Intent();
//                                Intent intent = new Intent(activity.getApplication(), MainActivity.class);
                                intent.putExtra(SNACK_MESSAGE, "アカウントを更新しました。");
                                activity.setResult(RESULT_OK, intent);
                                activity.finish();
                            }
                        },  // 成功時
                        throwable -> {
                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                            if (activity != null && !cd.isDisposed()) {
                                Intent intent = new Intent();
                                if (throwable instanceof HttpException && ((HttpException) throwable).code() == 409) {
                                    intent.putExtra(SNACK_MESSAGE, "アカウントの変更はありません。");
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
