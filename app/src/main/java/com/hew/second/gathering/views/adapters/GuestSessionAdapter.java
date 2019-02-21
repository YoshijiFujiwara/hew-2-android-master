package com.hew.second.gathering.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.Session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuestSessionAdapter extends BaseAdapter {

    protected List<Session> list;

    static public class ViewHolder {
        TextView title;
        TextView shop_name;
        TextView time;
        TextView count_member;
    }
    protected GuestSessionAdapter(){
        list = null;
    }
    public GuestSessionAdapter(ArrayList<Session> names){
        list =names;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.session_cell, parent, false);
            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.title);
            holder.shop_name = convertView.findViewById(R.id.shop_name);
            holder.time = convertView.findViewById(R.id.time);
            holder.count_member = convertView.findViewById(R.id.count_member);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(list.get(position).name);
        holder.shop_name.setText(list.get(position).name);
        holder.time.setText(list.get(position).name);
        holder.count_member.setText(list.get(position).users.size() + "");

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

    public List<Session> getList() { return list; }

    public void clear(){
        list.clear();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
}
