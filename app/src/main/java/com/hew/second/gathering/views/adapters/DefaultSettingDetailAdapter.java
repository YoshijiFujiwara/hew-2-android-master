package com.hew.second.gathering.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultSettingDetailAdapter extends BaseAdapter {

    private List<Data> list;

    static class ViewHolder {
        Spinner name;

    }
    static public class Data {
        public Data(int id, String name) {
            this.id = id;
            this.name = name;
        }
        public int id;
        public String name;
    }

    public DefaultSettingDetailAdapter(Data[] names){
        list = Arrays.asList(names);
    }
    public DefaultSettingDetailAdapter(ArrayList<Data> names){
        list = names;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_spinner_item, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.group_spinner);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

//        holder.name.set(list.get(position).name);

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
    public List<Data> getList(){
        return list;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
}
