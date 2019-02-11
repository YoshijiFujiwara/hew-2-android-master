package com.example.eventer.member.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.eventer.member.R;

import java.util.ArrayList;

public class MemberAdapter extends BaseAdapter {

    protected MemberAdapter.Data[] list;

    static public class ViewHolder {
        TextView userName;
        TextView uniqueName;
    }
    static public class Data {
        public Data(int id, String uniqueName ,String userName) {
            this.id = id;
            this.uniqueName = uniqueName;
            this.userName = userName;
        }

        public int getId() {
            return id;
        }

        public String getUniqueName() {
            return uniqueName;
        }

        public String getUserName() {
            return userName;
        }

        int id;
        String uniqueName;
        String userName;
    }
    protected MemberAdapter(){
        list = new MemberAdapter.Data[0];
    }
    public MemberAdapter(MemberAdapter.Data[] names){
        list = names;
    }
    public MemberAdapter(ArrayList<MemberAdapter.Data> names){
        list = names.toArray(new MemberAdapter.Data[0]);
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
