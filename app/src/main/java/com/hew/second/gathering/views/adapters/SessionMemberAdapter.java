package com.hew.second.gathering.views.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.SessionUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SessionMemberAdapter extends BaseAdapter {

    protected List<SessionUser> list;

    static public class ViewHolder {
        TextView userName;
        TextView uniqueName;
        TextView deleteButton;
    }
    protected SessionMemberAdapter(){
        list = null;
    }
    public SessionMemberAdapter(SessionUser[] names){
        list = Arrays.asList(names);
    }
    public SessionMemberAdapter(ArrayList<SessionUser> names){
        list = new ArrayList<>(names);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        GroupMemberAdapter.ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.invite_member_cell, parent, false);
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
        String status = list.get(position).join_status;
        holder.deleteButton.setTextSize(30.0f);
        holder.deleteButton.setTypeface(Typeface.DEFAULT);
        if(status.equals("allow")){
            holder.deleteButton.setText("◯");
            holder.deleteButton.setTextSize(20.0f);
            holder.deleteButton.setTypeface(Typeface.DEFAULT_BOLD);
            holder.deleteButton.setTextColor(Color.BLUE);
        }else if(status.equals("wait")){
            holder.deleteButton.setText("-");
            holder.deleteButton.setTextColor(Color.BLACK);
        }else{
            holder.deleteButton.setText("×");
            holder.deleteButton.setTextColor(Color.RED);
        }
        holder.deleteButton.setOnClickListener((view) -> {
            ((ListView) parent).performItemClick(view, position, R.id.member_delete);
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

    public List<SessionUser> getList() { return list; }

    public void clear(){
        list.clear();
    }
    public void addAll(List<SessionUser> friends){
        list.addAll(friends);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
}
