package com.hew.second.gathering.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Session;

import java.util.ArrayList;

public class SessionAdapter extends BaseAdapter {

    private Session list[];

    public static class ViewHolder {
        TextView sessionName;
        TextView sessionDate;
        TextView sessionPople;
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
                    .inflate(R.layout.fragment_session_text,parent,false);
            holder = new SessionAdapter.ViewHolder();
            holder.sessionName = convertView.findViewById(R.id.session_name);
            holder.sessionDate = convertView.findViewById(R.id.session_date);
            convertView.setTag(holder);
        } else {

            holder = (SessionAdapter.ViewHolder)convertView.getTag();

        }
        holder.sessionName.setText(list[position].name);
        holder.sessionDate.setText(list[position].start_time);
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
