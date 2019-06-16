package com.shnuedu.fragmentpage;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shnuedu.customControl.DeviceAdapter;
import com.shnuedu.goodmother.R;
import com.shnuedu.tools.Device;
import com.shnuedu.tools.MessageBox;
import com.shnuedu.tools.NetworkHelp;

public class DeviceFragment extends Fragment implements NetworkHelp.OnNetReceiveListener {

    private ListView deviceListView; //设备列表
    private DeviceAdapter adapter;//自定的适配器
    private Button btnSearch;
    private ProgressBar refreshPg;//刷新的图标

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;

    private OnFragmentInteractionListener mListener;
    private LayoutInflater inflater;
    private ViewGroup container;
    final NetworkHelp networkHelp = NetworkHelp.getInstance();

    public DeviceFragment() {
        networkHelp.onAttach(this);
    }

    public static DeviceFragment newInstance(String param1) {
        DeviceFragment fragment = new DeviceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        View rootView = inflater.inflate(R.layout.fragment_device, container, false);

        btnSearch = (Button) rootView.findViewById(R.id.search_btn_id);//搜索按钮
        btnSearch.setOnClickListener(btnSearch_Click);

        refreshPg = (ProgressBar) rootView.findViewById(R.id.refresh_pg_id);//刷新图标
        deviceListView = (ListView) rootView.findViewById(R.id.device_lv_id);//设备列表

        adapter = new DeviceAdapter(deviceListView.getContext());
        deviceListView.setAdapter(adapter);

        View device1 = inflater.inflate(R.layout.item_device_layout, container, false);
        ((TextView) device1.findViewById(R.id.tv_item_id)).setText("设备一");
        ((ImageView) device1.findViewById(R.id.wifi_iv_id)).setOnClickListener(imgOnClickListener);
        ((ImageView) device1.findViewById(R.id.detail_iv_id)).setOnClickListener(imgOnClickListener);

        View device2 = inflater.inflate(R.layout.item_device_layout, container, false);
        ((TextView) device2.findViewById(R.id.tv_item_id)).setText("设备二");
        ((ImageView) device2.findViewById(R.id.wifi_iv_id)).setOnClickListener(imgOnClickListener);
        ((ImageView) device2.findViewById(R.id.detail_iv_id)).setOnClickListener(imgOnClickListener);

        adapter.addItem(device1);
        adapter.addItem(device2);

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onDeviceFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onUdpReceiveListener(Device device) {
        System.out.println("收到Udp信息");
        View deviceView = inflater.inflate(R.layout.item_device_layout, container, false);
        ((TextView) deviceView.findViewById(R.id.tv_item_id)).setText(device.getName());
        ((ImageView) deviceView.findViewById(R.id.wifi_iv_id)).setOnClickListener(imgOnClickListener);
        ((ImageView) deviceView.findViewById(R.id.detail_iv_id)).setOnClickListener(imgOnClickListener);
        deviceListView.addFooterView(deviceView);
    }

    @Override
    public void onTcpReceiveListener(String msg) {
        System.out.println("收到Tcp信息:" + msg);
    }

    public interface OnFragmentInteractionListener {
        void onDeviceFragmentInteraction(Uri uri);
    }

    //搜索设备
    View.OnClickListener btnSearch_Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (refreshPg.getVisibility() == View.GONE) {//如果是隐藏的状态
                refreshPg.setVisibility(View.VISIBLE);  //设置显示
                btnSearch.setText("停止搜索");
                adapter.removeAll(); //清空原来的设备
                adapter.notifyDataSetChanged(); //通知UI更新
                networkHelp.startSearchDevice();//开始搜索
            } else {//如果是显示的状态
                refreshPg.setVisibility(View.GONE);//设置隐藏
                btnSearch.setText("搜索");
                networkHelp.stopSearchDevice();
            }
        }
    };

    private ImageView.OnClickListener imgOnClickListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View v) {
            TextView textView = (TextView) ((View) v.getParent()).findViewById(R.id.tv_item_id);
            switch (v.getId()) {
                case R.id.wifi_iv_id:
                    MessageBox.show(getContext(), textView.getText() + "Wifi", "设备");
                    break;
                case R.id.detail_iv_id:
                    MessageBox.show(getContext(), textView.getText() + "详情", "设备");
                    break;
            }
        }
    };
}
