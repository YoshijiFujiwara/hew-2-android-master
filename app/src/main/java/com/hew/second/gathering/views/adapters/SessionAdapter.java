package com.hew.second.gathering.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.api.Session;
import com.hew.second.gathering.hotpepper.Shop;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SessionAdapter extends BaseAdapter {

    protected List<Session> list;
    protected List<Shop> shopList = null;
    protected List<String> headerList = null;

    static public class ViewHolder {
        TextView title;
        TextView shop_name;
        TextView time;
        TextView count_member;
        TextView header;
        ImageView imageView;
    }

    protected SessionAdapter() {
        list = null;
    }

    public SessionAdapter(ArrayList<Session> names, List<Shop> shops, List<String> headers) {
        list = new ArrayList<>(names);
        shopList = new ArrayList<>(shops);
        headerList = new ArrayList<>(headers);
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
        if (list.get(position).start_time == null) {
            holder.time.setText("未定");
        } else {
            holder.time.setText(list.get(position).start_time + "〜");
        }
        holder.count_member.setText(list.get(position).users.size() + 1 + "名");

        if (shopList != null) {
            Picasso.get()
                    .load(shopList.get(position).photo.pc.l)
                    .fit()
                    .centerInside()
                    .into(holder.imageView);
        }
        if (position != 0 && headerList.get(position).equals(headerList.get(position - 1))) {
            holder.header.setVisibility(View.GONE);
        } else {
            holder.header.setVisibility(View.VISIBLE);
            holder.header.setText(headerList.get(position));
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
