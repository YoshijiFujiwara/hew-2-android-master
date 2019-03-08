package com.hew.second.gathering.views.adapters;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Friend;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.api.SessionUser;
import com.hew.second.gathering.hotpepper.Shop;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GuestSessionAdapter extends BaseAdapter {

    protected List<Session> list;
    protected List<Shop> shopList = null;

    static public class ViewHolder {
        TextView title;
        TextView shop_name;
        TextView time;
        TextView count_member;
        TextView header;
        ImageView imageView;
    }

    protected GuestSessionAdapter() {
        list = null;
    }

    public GuestSessionAdapter(ArrayList<Session> names, List<Shop> shops) {
        list = new ArrayList<>(names);
        shopList = new ArrayList<>(shops);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.session_cell, parent, false);
            holder = new ViewHolder();
            holder.title = convertView.findViewById(R.id.title);
            holder.shop_name = convertView.findViewById(R.id.shop_name);
            holder.time = convertView.findViewById(R.id.time);
            holder.count_member = convertView.findViewById(R.id.count_member);
            holder.imageView = convertView.findViewById(R.id.imageView_shop);
            holder.header = convertView.findViewById(R.id.header);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(list.get(position).name);
        holder.shop_name.setText(shopList.get(position).name);
        // 時刻表示フォーマット
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat output = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        SimpleDateFormat outputShort = new SimpleDateFormat("HH:mm", Locale.getDefault());
        StringBuilder strTime = new StringBuilder();
        String strStartTime = list.get(position).start_time;
        String strEndTime = list.get(position).end_time;
        if(list.get(position).start_time != null){
            try{
                Date start = sdf.parse(strStartTime);
                strTime.append(output.format(start));
                strTime.append("〜");
            }catch (Exception e){
                e.printStackTrace();
            }
        } else {
            strTime.append("未定");
        }
        holder.time.setText(strTime.toString());

        int ok = 0;
        ArrayList<SessionUser> users = new ArrayList<>(list.get(position).users);
        for (SessionUser u : users) {
            if (u.join_status.equals("allow")) {
                ok++;
            }
        }
        holder.count_member.setText(Integer.toString(ok + 1) + " / " + (users.size() + 1) + "人");

        if (shopList != null) {
            Picasso.get()
                    .load(shopList.get(position).photo.pc.l)
                    .fit()
                    .centerInside()
                    .into(holder.imageView);
        }
        holder.header.setVisibility(View.GONE);
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

    public List<Session> getList() {
        return list;
    }

    public void clear() {
        list.clear();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
