package com.hew.second.gathering.views.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.hew.second.gathering.R;
import com.hew.second.gathering.SearchArgs;
import com.hew.second.gathering.api.GroupUser;
import com.hew.second.gathering.hotpepper.Shop;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ShopListAdapter extends BaseAdapter {
    private List<Shop> list;

    static class ViewHolder {
        ConstraintLayout layout;
        TextView name;
        TextView genre;
        TextView distance;
        ImageView imageView;
    }

    public ShopListAdapter(ArrayList<Shop> names) {
        list = names;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ShopListAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.shop_cell, parent, false);
            holder = new ShopListAdapter.ViewHolder();
            holder.layout = convertView.findViewById(R.id.shop_layout);
            holder.name = convertView.findViewById(R.id.shop_cell_name);
            holder.genre = convertView.findViewById(R.id.shop_cell_genre);
            holder.distance = convertView.findViewById(R.id.shop_cell_distance);
            holder.imageView = convertView.findViewById(R.id.shop_cell_image);
            convertView.setTag(holder);
        } else {
            holder = (ShopListAdapter.ViewHolder) convertView.getTag();
        }

        // 先頭三軒（おすすめ）は色を変える
        if(position < 3){
            holder.layout.setBackgroundColor(Color.argb(255,255,230,230));
        } else {
            holder.layout.setBackgroundColor(Color.argb(255,255,255,255));
        }

        holder.name.setText(list.get(position).name);
        holder.genre.setText(list.get(position).genre.name);
        String d = Math.round(SearchArgs.getDistance(SearchArgs.lat, SearchArgs.lng, Float.valueOf(list.get(position).lat), Float.valueOf(list.get(position).lng))[0]) + "m";
        holder.distance.setText(d);

        Picasso.get()
                .load(list.get(position).photo.pc.l)
                .fit()
                .centerInside()
                .into(holder.imageView);
        return convertView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    public List<Shop> getList() {
        return list;
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
