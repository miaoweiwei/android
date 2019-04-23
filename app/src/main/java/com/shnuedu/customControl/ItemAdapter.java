package com.shnuedu.customControl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.shnuedu.goodmother.R;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> list_title;
    private List<Switch> list_switch;

    private TextView textTv;
    private Switch aSwitch;

    public ItemAdapter(Context context) {
        this.mContext = context;
        list_title = new ArrayList<>();
        list_switch = new ArrayList<>();
    }

    public void addItem(String title) {
        list_title.add(title);
    }

    @Override
    public int getCount() {
        return list_title.size();
    }

    @Override
    public Object getItem(int position) {
        return list_switch.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_switch_layout, null);

        textTv = convertView.findViewById(R.id.tv_item_id);
        aSwitch = convertView.findViewById(R.id.switch_item_id);

        textTv.setText(list_title.get(position));
        aSwitch.setTag(list_title.get(position));
        list_switch.add(aSwitch);
        return convertView;
    }
}
