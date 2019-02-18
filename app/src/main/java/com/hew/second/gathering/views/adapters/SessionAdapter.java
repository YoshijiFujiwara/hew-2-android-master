package com.hew.second.gathering.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionUser;

import java.util.ArrayList;

public class SessionAdapter extends BaseAdapter {

    private Session list[];
    private int usersList;

    public static class ViewHolder {
        TextView sessionName;
        TextView sessionDate;
        TextView sessionPeople;
    }
    public SessionAdapter(Session[] data) {
        list = data;
    }
    public SessionAdapter(ArrayList<Session> data) {
        list = data.toArray(new Session[0]);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SessionAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_session_cell,parent,false);
            holder = new SessionAdapter.ViewHolder();
            holder.sessionName = convertView.findViewById(R.id.session_name);
            holder.sessionDate = convertView.findViewById(R.id.session_date);
            holder.sessionPeople = convertView.findViewById(R.id.session_people);

            convertView.setTag(holder);
        } else {

            holder = (SessionAdapter.ViewHolder)convertView.getTag();

        }
        holder.sessionName.setText(list[position].name);
        holder.sessionDate.setText(list[position].start_time);
        holder.sessionPeople.setText(String.valueOf(list[position].users.size()));
        ArrayList<SessionUser> sessionUserList = (ArrayList<SessionUser>) list[position].users;

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
