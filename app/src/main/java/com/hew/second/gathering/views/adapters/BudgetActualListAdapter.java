package com.hew.second.gathering.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.media.Image;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hew.second.gathering.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class BudgetActualListAdapter extends ArrayAdapter {

    private final Activity context;
    private final String[] nameArray;
    private final Integer[] costArray;
    private final Boolean[] paidArray;
    private final String[] userIdArray; // hidden プロパティ的な

    public BudgetActualListAdapter(Activity context, String[] nameArrayParam, Integer[] costArrayParam, Boolean[] paidArrayParam, String[] userIdParam) {
        super(context, R.layout.listview_actual_row, userIdParam);

        this.context = context;
        this.nameArray = nameArrayParam;
        this.costArray = costArrayParam;
        this.paidArray = paidArrayParam;
        this.userIdArray = userIdParam;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.listview_actual_row, null,true);

        //this code gets references to objects in the listview_actual_row.xml file
        TextView nameTextField = (TextView) rowView.findViewById(R.id.budgetActualUsername);
        TextView infoTextField = (TextView) rowView.findViewById(R.id.budgetActualListInfo);
        TextView userIdField = (TextView) rowView.findViewById(R.id.budgetActualListUserId);
        ImageView paidImageView = (ImageView) rowView.findViewById(R.id.budgetActualListPaid);

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(nameArray[position]);
        infoTextField.setText(costArray[position].toString() + "円");
        userIdField.setText(userIdArray[position]);
        if (paidArray[position] == true) {
            ((Animatable) paidImageView.getDrawable()).start();
        }

        return rowView;
    }

//    @Override
//    public boolean isEnabled(int position) {
//        return false;
//    }
}
