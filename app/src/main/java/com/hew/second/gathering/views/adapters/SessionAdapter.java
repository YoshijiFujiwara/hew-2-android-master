package com.hew.second.gathering.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SessionAdapter extends BaseAdapter {

    private List<Session> list;
    private int usersList;

    public static class ViewHolder {
        TextView sessionName;
        TextView sessionDate;
        TextView sessionPeople;
        ImageView sessionImage;
        String startDate;
        String emdDate;
    }
    public SessionAdapter(Session[] data) {
        list =  Arrays.asList(data);
    }
    public SessionAdapter(ArrayList<Session> data) {
        list = new ArrayList<>(data);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SessionAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_session_cell,parent,false);
            holder = new SessionAdapter.ViewHolder();
            holder.sessionName = convertView.findViewById(R.id.session_site);
            holder.sessionDate = convertView.findViewById(R.id.session_date);
            holder.sessionPeople = convertView.findViewById(R.id.session_people);
            holder.sessionImage = convertView.findViewById(R.id.session_image_her);

            convertView.setTag(holder);
        } else {

            holder = (SessionAdapter.ViewHolder)convertView.getTag();

        }
        holder.sessionName.setText(list.get(position).name);
        holder.sessionDate.setText(list.get(position).start_time);
        holder.sessionPeople.setText(String.valueOf(list.get(position).users.size()) + "人");
        ArrayList<SessionUser> sessionUserList = (ArrayList<SessionUser>) list.get(position).users;

        for (int i = 0; i < list.get(position).users.size(); i++) {
            if (list.get(position).users.get(i).paid != 1) {
                holder.sessionImage.setImageResource(R.drawable.ic_warning);
            }

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

    @Override
    public long getItemId(int position) {
        return position;
    }


}
