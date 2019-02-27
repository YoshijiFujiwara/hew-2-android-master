package com.hew.second.gathering.views.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.EventProcessMainActivity;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.SessionDetail;
import com.hew.second.gathering.api.SessionUserDetail;
import com.hew.second.gathering.api.Util;

import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class BudgetEstimateListAdapter extends ArrayAdapter {
    private final Activity context;
    private final String[] nameArray;
    private final Integer[] costArray;
    private final Integer[] plusMinusArray;
    private final String[] attributeArray;
    private final String[] userIdArray;
    private final Integer sessionId;
    private final CompositeDisposable cd = new CompositeDisposable();

    public BudgetEstimateListAdapter(Activity context, String[] nameArrayParam, Integer[] costArrayParam,
                                     Integer[] plusMinusParam, String[] attributeParam, String[] userIdParam, int sessionId) {
        super(context, R.layout.listview_estimate_row, userIdParam);

        this.context = context;
        this.nameArray = nameArrayParam;
        this.costArray = costArrayParam;
        this.plusMinusArray = plusMinusParam;
        this.attributeArray = attributeParam;
        this.userIdArray = userIdParam;
        this.sessionId = sessionId;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.listview_estimate_row, null,true);

        //this code gets references to objects in the listview_actual_row.xml file
        TextView nameTextField = (TextView) rowView.findViewById(R.id.budgetEstimateUsername);
        TextView costTextField = (TextView) rowView.findViewById(R.id.budgetEstimateListCost);
        EditText plusMinusEditText = (EditText) rowView.findViewById(R.id.budgetEstimateListPlusMinus);
        TextView plusMinusEditTextLabel = (TextView) rowView.findViewById(R.id.budgetEstimateListPlusMinusLabel);
        TextView attributeTextField = (TextView) rowView.findViewById(R.id.budgetEstimateListAttribute);
        TextView userIdField = (TextView) rowView.findViewById(R.id.budgetEstimateListUserId);

//        // Edittext event listener
//        plusMinusEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) {
//                    Log.v("focus", String.valueOf(v.getId()));
//                    updateSessionUserPlusMinus(userIdArray[position], plusMinusEditText.getText().toString());
//                }
//            }
//        });

        plusMinusEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSessionUserPlusMinus(userIdArray[position], plusMinusEditText.getText().toString());

            }
        });


        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(nameArray[position]);
        costTextField.setText(costArray[position].toString() + "円");
        // 幹事の場合は、0固定で編集できないようにする
        if (position == 0) {
            plusMinusEditText.setText("");
            plusMinusEditText.setAlpha(0);
            plusMinusEditText.setInputType(InputType.TYPE_NULL);
        } else {
            plusMinusEditTextLabel.setText("±");
            plusMinusEditText.setText(plusMinusArray[position].toString());
        }

        attributeTextField.setText(attributeArray[position]);
        userIdField.setText(userIdArray[position]);

        return rowView;
    }

    /**
     * セッションユーザーの増減費を更新する
     */
    private void updateSessionUserPlusMinus(String userId, String plusMinus) {
        ApiService service = Util.getService();
        HashMap<String, String> body = new HashMap<>();
        body.put("plus_minus", plusMinus);
        Observable<SessionUserDetail> token = service.updateSessionUser(LoginUser.getToken(), sessionId, Integer.parseInt(userId), body);
        cd.add(token.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .unsubscribeOn(Schedulers.io())
            .subscribe(
                list -> {

                },  // 成功時
                throwable -> {
                    Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                }
            ));
    }
}
