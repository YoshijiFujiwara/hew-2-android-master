package com.hew.second.gathering.views.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InviteGroupAdapter extends BaseAdapter {

    private List<Group> list;
    private List<Boolean> checked;

    public boolean getChecked(int pos) {
        return checked.get(pos);
    }
    public Integer getCheckedPos() {
        for(int i = 0;i<checked.size();i++){
            if(checked.get(i)){
                return i;
            }
        }
        return null;
    }

    public void setChecked(int pos,Boolean checked) {
        this.checked.set(pos,checked);
    }

    public void clearChecked(){
        checked = new ArrayList<>();
        for (Group g: list){
            checked.add(false);
        }
    }

    static class ViewHolder {
        TextView name;
        TextView detail;
        TextView deleteButton;
    }

    public InviteGroupAdapter(ArrayList<Group> names){
        list = new ArrayList<>(names);
        checked = new ArrayList<>();
        for (Group g: list){
            checked.add(false);
        }
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
        holder.detail.setText(list.get(position).users.size() + "名");
        holder.deleteButton.setOnClickListener((view) -> {
            ((GridView) parent).performItemClick(view, position, R.id.delete_group);
        });
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
    public List<Group> getList(){
        return list;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
}
