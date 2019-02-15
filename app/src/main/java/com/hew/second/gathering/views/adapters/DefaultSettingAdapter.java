package com.hew.second.gathering.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.DefaultSetting;

import java.util.ArrayList;

public class DefaultSettingAdapter extends BaseAdapter {

    private Data[] list;

    static class ViewHolder {
        TextView name;
    }
    static public class Data {
        public Data(int id, String name) {
            this.id = id;
            this.name = name;
           }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        int id;
        String name;
    }

    public DefaultSettingAdapter(Data[] names){
        list = names;
    }
    public DefaultSettingAdapter(ArrayList<Data> names){
        list = names.toArray(new Data[0]);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.default_cell, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.default_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(list[position].name);

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
