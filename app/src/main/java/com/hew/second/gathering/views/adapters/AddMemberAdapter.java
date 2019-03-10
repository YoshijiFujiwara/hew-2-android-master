package com.hew.second.gathering.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Friend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddMemberAdapter extends BaseAdapter {

    protected List<Friend> list;

    static public class ViewHolder {
        TextView userName;
        TextView uniqueName;
        TextView deleteButton;
    }
    protected AddMemberAdapter(){
        list = null;
    }
    public AddMemberAdapter(Friend[] names){
        list = Arrays.asList(names);
    }
    public AddMemberAdapter(ArrayList<Friend> names){
        list = new ArrayList<>(names);
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
            holder.deleteButton = convertView.findViewById(R.id.member_delete);
            convertView.setTag(holder);
        } else {
            holder = (GroupMemberAdapter.ViewHolder) convertView.getTag();
        }
        if(list.get(position) == null){
            convertView.setVisibility(View.GONE);
            return convertView;
        }
        convertView.setVisibility(View.VISIBLE);

        holder.userName.setText(list.get(position).username);
        holder.uniqueName.setText(list.get(position).unique_id);
        holder.deleteButton.setVisibility(View.GONE);

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

    public List<Friend> getList() { return list; }

    public void clear(){
        list.clear();
    }
    public void addAll(List<Friend> friends){
        list.addAll(friends);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
}
