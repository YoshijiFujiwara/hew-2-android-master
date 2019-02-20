package com.hew.second.gathering.views.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hew.second.gathering.R;

public class BudgetEstimateListAdapter extends ArrayAdapter {
    private final Activity context;
    private final String[] nameArray;
    private final Integer[] infoArray;

    public BudgetEstimateListAdapter(Activity context, String[] nameArrayParam, Integer[] infoArrayParam) {
        super(context, R.layout.listview_estimate_row, nameArrayParam);

        this.context = context;
        this.nameArray = nameArrayParam;
        this.infoArray = infoArrayParam;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.listview_estimate_row, null,true);

        //this code gets references to objects in the listview_actual_row.xml file
        TextView nameTextField = (TextView) rowView.findViewById(R.id.budgetEstimateUsername);
        TextView infoTextField = (TextView) rowView.findViewById(R.id.budgetEstimateListInfo);

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(nameArray[position]);
        infoTextField.setText(infoArray[position].toString());

        return rowView;
    };
}
