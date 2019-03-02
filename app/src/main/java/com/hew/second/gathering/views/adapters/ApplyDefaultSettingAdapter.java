package com.hew.second.gathering.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.DefaultSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApplyDefaultSettingAdapter extends BaseAdapter {

    private List<DefaultSetting> list;

    static class ViewHolder {
        TextView name;
        TextView deleteButton;
    }

    public ApplyDefaultSettingAdapter(DefaultSetting[] names){
        list = Arrays.asList(names);
    }
    public ApplyDefaultSettingAdapter(ArrayList<DefaultSetting> names){
        list = new ArrayList<>(names);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.default_cell, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.default_name);
            holder.deleteButton= convertView.findViewById(R.id.delete_default);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.deleteButton.setVisibility(View.GONE);
        if(list.get(position) != null) {
            holder.name.setText(list.get(position).name);
        } else {
            holder.name.setText("適用しない");
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
    public List<DefaultSetting> getList(){
        return list;
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
}
