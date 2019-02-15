package com.hew.second.gathering.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hew.second.gathering.R;

import java.util.ArrayList;

public class GroupAdapter extends BaseAdapter {

    private Data[] list;

    static class ViewHolder {
        TextView name;
        TextView detail;
    }
    static public class Data {
        public Data(int id, String name, String detail) {
            this.id = id;
            this.name = name;
            this.detail = detail;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDetail() {
            return detail;
        }

        int id;
        String name;
        String detail;
    }

    public GroupAdapter(Data[] names){
        list = names;
    }
    public GroupAdapter(ArrayList<Data> names){
        list = names.toArray(new Data[0]);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.group_cell, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.member_name);
            holder.detail = convertView.findViewById(R.id.group_detail);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(list[position].name);
        holder.detail.setText(list[position].detail);

        return convertView;
    }

    @Override
    public int getCount() {
        return list.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
