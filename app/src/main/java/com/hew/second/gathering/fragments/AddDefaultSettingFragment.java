package com.hew.second.gathering.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.AddDefaultSettingActivity;
import com.hew.second.gathering.activities.EditGroupActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.DefaultSetting;
import com.hew.second.gathering.api.DefaultSettingDetail;
import com.hew.second.gathering.api.Util;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static android.app.Activity.RESULT_OK;
import static com.hew.second.gathering.activities.BaseActivity.INTENT_EDIT_DEFAULT;
import static com.hew.second.gathering.activities.BaseActivity.INTENT_EDIT_GROUP;
import static com.hew.second.gathering.activities.BaseActivity.SNACK_MESSAGE;


public class AddDefaultSettingFragment extends BaseFragment {
    private static final String MESSAGE = "message";
    int defaultSettingId = -1;

    private String spinnerItems[] = {"グループ1", "グループ2"};

    public static AddDefaultSettingFragment newInstance() {
        return new AddDefaultSettingFragment();
    }

    public static AddDefaultSettingFragment newInstance(String message) {
        AddDefaultSettingFragment fragment = new AddDefaultSettingFragment();

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
        view = inflater.inflate(R.layout.fragment_add_default,
                container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.setTitle("デフォルト追加");


//        Intent beforeIntent = activity.getIntent();
//        defaultSettingId = beforeIntent.getIntExtra("DEFAULTSETTING_ID", -1);//設定したkeyで取り出す

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

        Spinner spinner = getActivity().findViewById(R.id.group_spinner);

        // ArrayAdapter
        ArrayAdapter<String> adapter
                = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinnerItems);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // spinner に adapter をセット
        spinner.setAdapter(adapter);

        // リスナーを登録
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner)parent;
                String item = (String)spinner.getSelectedItem();
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });


        Button saveButton = activity.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // データ保存
               Util.setLoading(true,activity);
               createDefault();
            }
        });

    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        fetchList();
//    }
//
//    private void fetchList() {
//        ApiService service = Util.getService();
//        Observable<DefaultSettingDetail> token = service.getDefaultSettingDetail(LoginUser.getToken(), defaultSettingId);
//        cd.add(token.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .unsubscribeOn(Schedulers.io())
//                .subscribe(
//                        list -> {
//                            if (activity != null) {
//                                updateList(list.data);
//                            }
//                        },  // 成功時
//                        throwable -> {
//                            Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
//                            if (activity != null && !cd.isDisposed()) {
//                                activity.finish();
//                            }
//                        }
//                ));
//    }

    private void updateList(DefaultSetting gdi) {
        EditText defaultName = activity.findViewById(R.id.default_input);
        EditText startTime = activity.findViewById(R.id.start_time);

        defaultName.setText(gdi.name);
        startTime.setText(gdi.timer);
    }

//        Spinner spinner = getActivity().findViewById(R.id.group_spinner);
//
//        // ArrayAdapter
//        ArrayAdapter<String> adapter
//                = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, spinnerItems);
//
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//        // spinner に adapter をセット
//        spinner.setAdapter(adapter);
//
//        // リスナーを登録
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            //　アイテムが選択された時
//            @Override
//            public void onItemSelected(AdapterView<?> parent,
//                                       View view, int position, long id) {
//                Spinner spinner = (Spinner)parent;
//                String item = (String)spinner.getSelectedItem();
//            }
//
//            //　アイテムが選択されなかった
//            public void onNothingSelected(AdapterView<?> parent) {
//                //
//            }
//        });


    public void createDefault() {
        ApiService service = Util.getService();
        EditText defaultName = activity.findViewById(R.id.default_input);
        EditText startTime = activity.findViewById(R.id.start_time);
//        Spinner spinner = activity.findViewById(R.id.group_spinner);

//        textView.setText(item);
        HashMap<String, String> body = new HashMap<>();
        body.put("name", defaultName.getText().toString());
        body.put("timer", startTime.getText().toString());
//        body.put("group", spinner.getSelectedItem().toString());

        Observable<DefaultSettingDetail> token = service.createDefaultSetting(LoginUser.getToken(), body);
        cd.add(token.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(
                        list -> {
                            if (activity != null) {
                                Intent intent = new Intent(activity.getApplication(), AddDefaultSettingActivity.class);
                                intent.putExtra("DEFAULTSETTING_ID", list.data.id);
                                startActivityForResult(intent, INTENT_EDIT_DEFAULT);
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
}
