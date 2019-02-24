package com.hew.second.gathering.views.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hew.second.gathering.R;

import java.util.PrimitiveIterator;

public class BudgetEstimateListAdapter extends ArrayAdapter {
    private final Activity context;
    private final String[] nameArray;
    private final Integer[] costArray;
    private final Integer[] plusMinusArray;
    private final String[] attributeArray;

    public BudgetEstimateListAdapter(Activity context, String[] nameArrayParam, Integer[] costArrayParam, Integer[] plusMinusParam, String[] attributeParam) {
        super(context, R.layout.listview_estimate_row, nameArrayParam);

        this.context = context;
        this.nameArray = nameArrayParam;
        this.costArray = costArrayParam;
        this.plusMinusArray = plusMinusParam;
        this.attributeArray = attributeParam;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.listview_estimate_row, null,true);

        //this code gets references to objects in the listview_actual_row.xml file
        TextView nameTextField = (TextView) rowView.findViewById(R.id.budgetEstimateUsername);
        TextView costTextField = (TextView) rowView.findViewById(R.id.budgetEstimateListCost);
        TextView plusMinusTextField = (TextView) rowView.findViewById(R.id.budgetEstimateListPlusMinus);
        TextView attributeTextField = (TextView) rowView.findViewById(R.id.budgetEstimateListAttribute);

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(nameArray[position]);
        costTextField.setText(costArray[position].toString() + "円");
        plusMinusTextField.setText(plusMinusArray[position].toString());
        attributeTextField.setText(attributeArray[position]);

        return rowView;
    }

//    @Override
//    public boolean isEnabled(int position) {
//        return false;
//    }
}
