//package com.hew.second.gathering.views.adapters;
//
//import android.content.ClipData;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.BaseAdapter;
//import android.widget.GridView;
//import android.widget.Spinner;
//import android.widget.TextView;
//
//import com.hew.second.gathering.R;
//import com.hew.second.gathering.api.Group;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//public class DefaultSettingDetailAdapter extends BaseAdapter {
//
//    private List<Data> list;
//
//    static class ViewHolder {
//        Spinner group;
//    }
//    static public class Data {
//        public Data(int id, Group group) {
//            this.id = id;
//            this.group = group;
//        }
//
//        public int id;
//        public Group group;
//
//    }
//
//    public DefaultSettingDetailAdapter(Data[] names){
//        list = Arrays.asList(names);
//    }
//    public DefaultSettingDetailAdapter(ArrayList<Data> names){
//        list = names;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        ViewHolder holder;
//
//        if (convertView == null) {
//
//           convertView = LayoutInflater.from(parent.getContext())
//                    .inflate(android.R.layout.simple_spinner_item, parent, false);
//            holder = new ViewHolder();
//            holder.group = convertView.findViewById(R.id.group_spinner);
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//
//        holder.group.set(list.get(position).group);
//
//
//        return convertView;
//    }
//
//    @Override
//    public int getCount() {
//        return list.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return position;
//    }
//    public List<DefaultSettingDetailAdapter.Data> getList(){
//        return list;
//    }
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//}
