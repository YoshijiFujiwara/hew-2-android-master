package com.hew.second.gathering.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hew.second.gathering.R;

import java.util.ArrayList;


public class EventFragment extends Fragment {


    public static EventFragment newInstance() { return new EventFragment(); }

    private static class ViewHolder {
        ImageView shop_image;
        TextView title;
        TextView shop_name;
        TextView time;
        TextView count_member;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // ListViewに表示するデータ
        final ArrayList<String> items = new ArrayList<>();
        items.add("新入生歓迎会");
        items.add("同窓会");
        items.add("女子会");

        // ListViewをセット
        final ArrayAdapter adapter = new ArrayAdapter(this.getContext(), android.R.layout.simple_list_item_1, items);
        ListView listView = (ListView) view.findViewById(R.id.listView_event);
        listView.setAdapter(adapter);

    }

}
