package com.shnuedu.customControl;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends BaseAdapter {
    private Context mContext;
    private List<View> list_View;

    public DeviceAdapter(Context context) {
        this.mContext = context;
        list_View = new ArrayList<>();
    }

    public void addItem(View view) {
        list_View.add(view);
    }

    public View remove(int position) {
        return list_View.remove(position);
    }

    public void remove(View view) {
        list_View.remove(view);
    }

    public void removeAll() {
        list_View.clear();
    }

    @Override
    public int getCount() {
        return list_View.size();
    }

    @Override
    public Object getItem(int position) {
        return list_View.get(position);
    }

    @Override
    public long getItemId(int position) {
        return list_View.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = list_View.get(position);
        return convertView;
    }
}
