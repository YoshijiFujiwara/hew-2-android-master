package com.hew.second.gathering.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hew.second.gathering.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class BudgetActualListAdapter extends ArrayAdapter {

    private final Activity context;
    private final String[] nameArray;
    private final Integer[] costArray;

    public BudgetActualListAdapter(Activity context, String[] nameArrayParam, Integer[] costArrayParam) {
        super(context, R.layout.listview_actual_row, nameArrayParam);

        this.context = context;
        this.nameArray = nameArrayParam;
        this.costArray = costArrayParam;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.listview_actual_row, null,true);

        //this code gets references to objects in the listview_actual_row.xml file
        TextView nameTextField = (TextView) rowView.findViewById(R.id.budgetActualUsername);
        TextView infoTextField = (TextView) rowView.findViewById(R.id.budgetActualListInfo);

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(nameArray[position]);
        infoTextField.setText(costArray[position].toString());

        return rowView;
    };
}
