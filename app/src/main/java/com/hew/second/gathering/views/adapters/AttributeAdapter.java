package com.hew.second.gathering.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Attribute;
import com.hew.second.gathering.api.Friend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AttributeAdapter extends BaseAdapter {

    protected List<Attribute> list;

    static public class ViewHolder {
        TextView userName;
        TextView plus_minus;
        TextView deleteButton;
    }
    protected AttributeAdapter(){
        list = null;
    }
    public AttributeAdapter(Attribute[] names){
        list = Arrays.asList(names);
    }
    public AttributeAdapter(ArrayList<Attribute> names){
        list = new ArrayList<>(names);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.attribute_cell, parent, false);
            holder = new ViewHolder();
            holder.userName = convertView.findViewById(R.id.attribute_name);
            holder.plus_minus = convertView.findViewById(R.id.attribute_plus_minus);
            holder.deleteButton = convertView.findViewById(R.id.attribute_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.userName.setText(list.get(position).name);
        holder.plus_minus.setText(list.get(position).plus_minus == null? "¥0" : "¥" + list.get(position).plus_minus.toString());
        holder.deleteButton.setOnClickListener((view) -> {
            ((ListView) parent).performItemClick(view, position, R.id.attribute_delete);
        });

        return convertView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    public List<Attribute> getList() { return list; }

    public void clear(){
        list.clear();
    }
    public void addAll(List<Attribute> attributes){
        list.addAll(attributes);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
}
