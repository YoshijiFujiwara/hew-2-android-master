package com.hew.second.gathering.views.adapters;

import android.graphics.Color;
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

public class GroupMemberListAdapter extends BaseAdapter {

    protected List<Friend> list;
    private List<Boolean> checked;
    static public class ViewHolder {
        TextView userName;
        TextView uniqueName;
        TextView deleteButton;
    }
    public boolean getChecked(int pos) {
        return checked.get(pos);
    }
    public List<Boolean> getCheckedList() {
        return checked;
    }
    public Integer getCheckedCount() {
        Integer i = 0;
        for(Boolean b : checked){
            if(b){
                i++;
            }
        }
        return i;
    }
    public void setChecked(int pos,Boolean checked) {
        this.checked.set(pos,checked);
    }

    public void clearChecked(){
        checked = new ArrayList<>();
        for (Friend g: list){
            checked.add(false);
        }
    }
    public GroupMemberListAdapter(ArrayList<Friend> names){
        list = new ArrayList<>(names);
        checked = new ArrayList<>();
        for (Friend g: list){
            checked.add(false);
        }
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

        holder.userName.setText(list.get(position).username);
        holder.uniqueName.setText(list.get(position).unique_id);
        holder.deleteButton.setVisibility(View.GONE);
        if(checked.get(position)){
            convertView.setBackgroundColor(Color.parseColor("#70B98E70"));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }
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
