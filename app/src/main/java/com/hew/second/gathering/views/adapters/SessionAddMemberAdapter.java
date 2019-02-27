package com.hew.second.gathering.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.GroupUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SessionAddMemberAdapter extends BaseAdapter {
    private List<Friend> list;
    static class ViewHolder extends MemberAdapter.ViewHolder {
        TextView deleteButton;
    }
    public SessionAddMemberAdapter(Friend[] names){
        list = Arrays.asList(names);
    }
    public SessionAddMemberAdapter(ArrayList<Friend> names){
        list = new ArrayList<>(names);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SessionAddMemberAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.group_member_cell, parent, false);
            holder = new SessionAddMemberAdapter.ViewHolder();
            holder.userName = convertView.findViewById(R.id.group_member_name);
            holder.uniqueName = convertView.findViewById(R.id.group_member_unique_name);
            holder.deleteButton = convertView.findViewById(R.id.delete_group_member);
            convertView.setTag(holder);
        } else {
            holder = (SessionAddMemberAdapter.ViewHolder) convertView.getTag();
        }
        holder.userName.setText(list.get(position).username);
        holder.uniqueName.setText(list.get(position).unique_id);
        holder.deleteButton.setVisibility(View.GONE);
        return convertView;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    public List<Friend> getList(){
        return list;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }


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
