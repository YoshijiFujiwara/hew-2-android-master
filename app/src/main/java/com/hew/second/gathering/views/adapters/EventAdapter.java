package com.hew.second.gathering.views.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.GridView;
import android.widget.TextView;

import com.hew.second.gathering.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventAdapter extends BaseAdapter {

    private List<Data> list;

    static class ViewHolder {
        TextView name;
        TextView shop_id;
        TextView start_time;
        TextView users;
    }
    static public class Data {
        public Data(int id, String name, String shop_id, String start_time, String users) {
            this.id = id;
            this.name = name;
            this.shop_id = shop_id;
            this.start_time = start_time;
            this.users = users;
        }
        public int id;
        public String name;
        public String shop_id;
        public String start_time;
        public String users;
    }

    public EventAdapter(Data[] names){
        list = Arrays.asList(names);
    }
    public EventAdapter(ArrayList<Data> names){
        list = new ArrayList<>(names);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.event_cell, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.title);
            holder.shop_id = convertView.findViewById(R.id.shop_name);
            holder.start_time= convertView.findViewById(R.id.time);
            holder.users= convertView.findViewById(R.id.count_member);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.name.setText(list.get(position).name);
        holder.shop_id.setText(String.valueOf(list.get(position).shop_id));
        holder.start_time.setText(list.get(position).start_time);
        holder.users.setText(list.get(position).users);
//        holder.button.setOnClickListener((view) -> {
//            ((GridView) parent).performItemClick(view, position, R.id.delete_group);
//        });

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
    public List<Data> getList(){
        return list;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
}
