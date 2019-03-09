package com.hew.second.gathering.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.media.Image;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hew.second.gathering.LogUtil;
import com.hew.second.gathering.LoginUser;
import com.hew.second.gathering.R;
import com.hew.second.gathering.activities.LoginActivity;
import com.hew.second.gathering.api.ApiService;
import com.hew.second.gathering.api.SessionUserDetail;
import com.hew.second.gathering.api.Util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

public class BudgetActualListAdapter extends ArrayAdapter {

    private final Activity context;
    private final String[] nameArray;
    private final Integer[] costArray;
    private final Boolean[] paidArray;
    private final String[] userIdArray; // hidden プロパティ的な
    private final Integer sessionId;
    private final CompositeDisposable cd = new CompositeDisposable();

    public BudgetActualListAdapter(Activity context, String[] nameArrayParam, Integer[] costArrayParam, Boolean[] paidArrayParam, String[] userIdParam, int sessionId) {
        super(context, R.layout.listview_actual_row, userIdParam);

        this.context = context;
        this.nameArray = nameArrayParam;
        this.costArray = costArrayParam;
        this.paidArray = paidArrayParam;
        this.userIdArray = userIdParam;
        this.sessionId = sessionId;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.listview_actual_row, null,true);

        // this code gets references to objects in the listview_actual_row.xml file
        TextView nameTextField = (TextView) rowView.findViewById(R.id.budgetActualUsername);
        TextView infoTextField = (TextView) rowView.findViewById(R.id.budgetActualListInfo);
        TextView userIdField = (TextView) rowView.findViewById(R.id.budgetActualListUserId);
        ImageView paidImageView = (ImageView) rowView.findViewById(R.id.budgetActualListPaid);

        paidImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position != 0) {
                    switchPaid(userIdArray[position]);
                }
                if (paidArray[position] == true) {
                    paidImageView.setImageResource(R.drawable.ic_check_grey);
                    paidArray[position] = false;
                } else if (position != 0){
                    paidImageView.setImageResource(R.drawable.ic_check_green);
                    paidArray[position] = true;
                }
            }
        });

        // this code sets the values of the objects to values from the arrays
        nameTextField.setText(nameArray[position]);
        infoTextField.setText(String.format("%,d", costArray[position]) + "円");
        userIdField.setText(userIdArray[position]);
        // 幹事の場合
        if (position == 0) {
            paidImageView.setAlpha(0);
        }
        if (paidArray.length > 0 && paidArray[position] == true) {
            paidImageView.setImageResource(R.drawable.ic_check_green);
        } else {
            paidImageView.setImageResource(R.drawable.ic_check_grey);
        }

        return rowView;
    }

    /**
     * 指定したuserの支払い状況を反転する処理
     * @param userId
     */
    private void switchPaid(String userId) {
        ApiService service = Util.getService();
        Observable<SessionUserDetail> token = service.sessionUserSwitchPaid(sessionId, Integer.parseInt(userId));
        cd.add(token.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .unsubscribeOn(Schedulers.io())
            .subscribe(
                list -> {
                    
                },
                throwable -> {
                    Log.d("api", "API取得エラー：" + LogUtil.getLog() + throwable.toString());
                    if (!cd.isDisposed()) {
                        if (throwable instanceof HttpException && (((HttpException) throwable).code() == 401 || ((HttpException) throwable).code() == 500)) {

                        }
                    }
                }
            ));
    }
}
