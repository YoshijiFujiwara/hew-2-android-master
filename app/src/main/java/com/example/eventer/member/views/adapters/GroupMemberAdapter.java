package com.example.eventer.member.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.eventer.member.R;

import java.util.ArrayList;

public class GroupMemberAdapter extends MemberAdapter {
    public GroupMemberAdapter(MemberAdapter.Data[] names){
        list = names;
    }
    public GroupMemberAdapter(ArrayList<MemberAdapter.Data> names){
        list = names.toArray(new MemberAdapter.Data[0]);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GroupMemberAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.group_member_cell, parent, false);
            holder = new GroupMemberAdapter.ViewHolder();
            holder.userName = convertView.findViewById(R.id.group_member_name);
            holder.uniqueName = convertView.findViewById(R.id.group_member_unique_name);
            convertView.setTag(holder);
        } else {
            holder = (GroupMemberAdapter.ViewHolder) convertView.getTag();
        }
        holder.userName.setText(list[position].userName);
        holder.uniqueName.setText(list[position].uniqueName);
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
