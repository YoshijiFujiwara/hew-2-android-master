package com.hew.second.gathering.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Friend;

import java.util.ArrayList;

public class MemberAdapter extends BaseAdapter {

    protected Friend[] list;

    static public class ViewHolder {
        TextView userName;
        TextView uniqueName;
    }
    protected MemberAdapter(){
        list = new Friend[0];
    }
    public MemberAdapter(Friend[] names){
        list = names;
    }
    public MemberAdapter(ArrayList<Friend> names){
        list = names.toArray(new Friend[0]);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        GroupMemberAdapter.ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.member_cell, parent, false);
            holder = new GroupMemberAdapter.ViewHolder();
            holder.userName = convertView.findViewById(R.id.member_name);
            holder.uniqueName = convertView.findViewById(R.id.member_unique_name);
            convertView.setTag(holder);
        } else {
            holder = (GroupMemberAdapter.ViewHolder) convertView.getTag();
        }

        holder.userName.setText(list[position].username);
        holder.uniqueName.setText(list[position].unique_id);

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
