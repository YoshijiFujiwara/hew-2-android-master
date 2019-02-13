package com.hew.second.gathering.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupAdapter extends BaseAdapter {

    private List<Data> list;

    static class ViewHolder {
        TextView name;
        TextView detail;
        TextView deleteButton;
    }
    static public class Data {
        public Data(int id, String name, String detail) {
            this.id = id;
            this.name = name;
            this.detail = detail;
        }
        public int id;
        public String name;
        public String detail;
    }

    public GroupAdapter(Data[] names){
        list = Arrays.asList(names);
    }
    public GroupAdapter(ArrayList<Data> names){
        list = names;
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
            holder.deleteButton= convertView.findViewById(R.id.delete_group);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(list.get(position).name);
        holder.detail.setText(list.get(position).detail);
        holder.deleteButton.setOnClickListener((view) -> {
            ((GridView) parent).performItemClick(view, position, R.id.delete_group);
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
    public List<Data> getList(){
        return list;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
}
