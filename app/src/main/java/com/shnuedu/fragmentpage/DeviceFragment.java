package com.shnuedu.fragmentpage;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.shnuedu.customControl.DeviceAdapter;
import com.shnuedu.goodmother.R;
import com.shnuedu.tools.Device;
import com.shnuedu.tools.DeviceSearchResult;
import com.shnuedu.tools.Message;
import com.shnuedu.tools.MessageBox;
import com.shnuedu.tools.NetMessage;
import com.shnuedu.tools.NetUtil;
import com.shnuedu.tools.NetworkHelp;

public class DeviceFragment extends Fragment {
    private boolean isLoad = false;

    private View rootView = null;
    private ListView deviceListView; //设备列表
    private DeviceAdapter adapter;//自定的适配器
    private Button btnSearch;
    private ProgressBar refreshPg;//刷新的图标

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;

    private OnFragmentInteractionListener mListener;

    private Handler handler = null;//用于夸线程更新UI
    private static final int UDPRECEIVE = 0;//UDP接收到信息
    private static final int TCPRECEIVE = 1;//TCP接收到信息

    private LayoutInflater inflater;
    private ViewGroup container;
    final NetworkHelp networkHelp = NetworkHelp.getInstance();

    public DeviceFragment() {
        networkHelp.addOnNetReceiveListener(netReceiveListener);
        handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case UDPRECEIVE:
                        Device device = (Device) msg.obj;
                        View deviceView = inflater.inflate(R.layout.item_device_layout, container, false);
                        String name = device.Name;
                        if (device.NetworkMode != Message.Network_Mode_Station)
                            name += "(未初始化)";
                        ((TextView) deviceView.findViewById(R.id.device_name_tv_id)).setText(name);
                        ((ImageView) deviceView.findViewById(R.id.detail_iv_id)).setOnClickListener(imgOnClickListener);
                        deviceView.setTag(device);
                        adapter.addItem(deviceView);
                        adapter.notifyDataSetChanged(); //通知UI更新设备列表
                        break;
                    case TCPRECEIVE:
                        MessageBox.show(getActivity(), msg.obj.toString(), "提示");
                        break;
                }
            }
        };
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
        if (!isLoad) {
            this.inflater = inflater;
            this.container = container;
            rootView = inflater.inflate(R.layout.fragment_device, container, false);

            btnSearch = (Button) rootView.findViewById(R.id.device_search_btn_id);//搜索按钮
            btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnSearch_Click();
                }
            });

            refreshPg = (ProgressBar) rootView.findViewById(R.id.device_refresh_pg_id);//刷新图标
            deviceListView = (ListView) rootView.findViewById(R.id.device_lv_id);//设备列表
            deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    btnConnectDevice_Click((View) adapter.getItem(position));
                }
            });
            adapter = new DeviceAdapter(deviceListView.getContext());
            deviceListView.setAdapter(adapter);
//            View device1 = inflater.inflate(R.layout.item_device_layout, container, false);
//            ((TextView) device1.findViewById(R.id.device_name_tv_id)).setText("设备一");
//            ((ImageView) device1.findViewById(R.id.detail_iv_id)).setOnClickListener(imgOnClickListener);
//            device1.setEnabled(false);
//
//            View device2 = inflater.inflate(R.layout.item_device_layout, container, false);
//            ((TextView) device2.findViewById(R.id.device_name_tv_id)).setText("设备二");
//            ((ImageView) device2.findViewById(R.id.detail_iv_id)).setOnClickListener(imgOnClickListener);
//
//            adapter.addItem(device1);
//            adapter.addItem(device2);
            isLoad = true;
        }
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onFragmentInteraction(Device args) {
        if (mListener != null) {
            mListener.onDeviceFragmentInteraction(args);
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
        networkHelp.removeOnNetReceiveListener(netReceiveListener);
    }

    public interface OnFragmentInteractionListener {
        void onDeviceFragmentInteraction(Device args);
    }

    //搜索设备
    private void btnSearch_Click() {
        if (!NetUtil.isWifiEnabled(rootView.getContext())) {
            MessageBox.show(rootView.getContext(), "请先打开wifi并连接到与吸乳器同一个wifi下", "提示");
            return;
        }
        if (!networkHelp.isSearchDevice()) {//如果不是正在搜索
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

    //连接设备
    private void btnConnectDevice_Click(View v) {
        Device device;
        if (v != null && (device = (Device) v.getTag()) != null) {
            Device tempDevice = networkHelp.getCurrentConnectDevice();
            if (device.NetworkMode != Message.Network_Mode_Station) { //未初始化
                if (tempDevice == null) {
                    networkHelp.connectDevice(device);
                } else {
                    if (tempDevice.Ip.equals(device.Ip)) {//设备已经连接
                        MessageBox.show(rootView.getContext(), "设备已经连接");
                    } else {//切换连接设备
                        networkHelp.closeTcpConnect();
                        networkHelp.connectDevice(device);
                    }
                }
                onFragmentInteraction(device);
            } else {//已经初始化
                if (tempDevice == null) {
                    networkHelp.connectDevice(device);
                } else {
                    if (tempDevice.Ip.equals(device.Ip)) {//设备已经连接
//                        onFragmentInteraction("设备已经连接");
                        MessageBox.show(rootView.getContext(), "设备已经连接");
                    } else {//切换连接设备
                        networkHelp.closeTcpConnect();
                        networkHelp.connectDevice(device);
                    }
                }
            }
        }
    }

    private ImageView.OnClickListener imgOnClickListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View v) {
            //当前这个View是详情这个按钮，要通过(View) v.getParent()获取父View就是一个设备Item
            Device device = (Device) ((View) v.getParent()).getTag();
//            TextView textView = ((View) v.getParent()).findViewById(R.id.device_name_tv_id);
            String msg = String.format("设备名称：%s\n" +
                            "设备电量：%s\n" +
                            "设备IP：%s\n" +
                            "设备Udp端口：%s\n" +
                            "设备Tcp端口：%s",
                    device.Name,
                    device.PowerBattery,
                    device.Ip,
                    device.UdpPort,
                    device.TcpPort);
            MessageBox.show(getContext(), msg, "设备信息");
        }
    };

    NetworkHelp.OnNetReceiveListener netReceiveListener = new NetworkHelp.OnNetReceiveListener() {
        @Override
        public void onUdpReceiveListener(Gson gson, NetMessage netMessage) {
            if (netMessage.MsgId != Message.SearchDevice_MsgId || !netMessage.MsgStatus) return;
            DeviceSearchResult result = netMessage.JsonToObject(gson, DeviceSearchResult.class);
            Device device = new Device();
            device.Name = result.Name;
            device.NetworkMode = result.NetworkMode;
            device.PowerBattery = result.Power;
            device.Ip = result.Ip;

            //处理完成后给handler发送消息
            android.os.Message msg = new android.os.Message();
            msg.what = UDPRECEIVE;
            msg.obj = device;
            handler.sendMessage(msg); //告诉主线程去更新UI
        }

        @Override
        public void onTcpReceiveListener(Gson gson, NetMessage netMessage) {
            if (netMessage.MsgId != Message.TcpStatus_MsgId || !netMessage.MsgStatus) return;
            String msgObj = netMessage.MsgObj.toString();
            if (msgObj.equals(Message.TcpStatus_Msg_Busy)) { //设备正在忙碌中
                networkHelp.closeTcpConnect(); // 关闭这个TCP连接
            }
            if (msgObj.equals(Message.TcpStatus_Msg_Idle)) {//设备空闲中
                //处理完成后给handler发送消息
                android.os.Message msg = new android.os.Message();
                msg.what = TCPRECEIVE;
                msg.obj = "设备连接成功！";
                handler.sendMessage(msg); //告诉主线程去更新UI
            }
        }
    };
}