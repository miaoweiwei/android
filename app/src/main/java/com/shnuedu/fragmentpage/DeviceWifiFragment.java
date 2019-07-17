package com.shnuedu.fragmentpage;

import android.content.Context;
import android.net.Uri;
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
import android.widget.Toast;

import com.google.gson.Gson;
import com.hb.dialog.myDialog.MyAlertInputDialog;
import com.shnuedu.customControl.DeviceAdapter;
import com.shnuedu.goodmother.R;
import com.shnuedu.tools.Message;
import com.shnuedu.tools.MessageBox;
import com.shnuedu.tools.NetMessage;
import com.shnuedu.tools.NetworkHelp;
import com.shnuedu.tools.Wifi;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeviceWifiFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeviceWifiFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceWifiFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private String mParam1;
    private OnFragmentInteractionListener mListener;

    private View rootView = null;
    private LayoutInflater inflater;
    private ViewGroup container;

    private Button btnScanWifi;
    private ProgressBar refreshPg;//刷新的图标
    private ListView wifiListView;
    private DeviceAdapter adapter;//自定的适配器


    private Handler handler = null;//用于夸线程更新UI
    private static final int SCANWIFI = 0;//UDP接收到信息
    private static final int CONNECTWIFI = 1;//TCP接收到信息

    final NetworkHelp networkHelp = NetworkHelp.getInstance();
    private boolean isLoad = false;

    public DeviceWifiFragment() {
        networkHelp.addOnNetReceiveListener(netReceiveListener);
        handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case SCANWIFI:
                        List<Wifi> wifis = (List<Wifi>) msg.obj;
                        for (Wifi wifi : wifis) {
                            View wifiView = inflater.inflate(R.layout.item_device_layout, container, false);
                            String name = wifi.Ssid;
                            ((TextView) wifiView.findViewById(R.id.device_name_tv_id)).setText(name);
                            ImageView wifiImg = ((ImageView) wifiView.findViewById(R.id.detail_iv_id));
                            if (wifi.Rssi > -60)
                                wifiImg.setBackground(getResources().getDrawable(R.mipmap.wifi_3));
                            else if (wifi.Rssi > -90)
                                wifiImg.setBackground(getResources().getDrawable(R.mipmap.wifi_2));
                            else
                                wifiImg.setBackground(getResources().getDrawable(R.mipmap.wifi_1));
                            wifiImg.setOnClickListener(imgOnClickListener);
                            wifiView.setTag(wifi);
                            adapter.addItem(wifiView);
                            adapter.notifyDataSetChanged(); //通知UI更新设备列表
                        }
                        break;
                    case CONNECTWIFI:
                        String obj = msg.obj.toString();
                        if (obj.equals(Message.ConnectWifi_Msg)) {
                            MessageBox.show(rootView.getContext(), "设备Wifi连接成功，请将手机也连如相同的wifi", "提示");
                            networkHelp.closeTcpConnect();
                        }

                        break;
                }
            }
        };
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment DeviceWifiFragment.
     */
    public static DeviceWifiFragment newInstance(String param1) {
        DeviceWifiFragment fragment = new DeviceWifiFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!isLoad) {
            this.inflater = inflater;
            this.container = container;
            rootView = inflater.inflate(R.layout.fragment_device_wifi, container, false);
            btnScanWifi = rootView.findViewById(R.id.wifi_search_btn_id);
            btnScanWifi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnScanWifi_Click(v);
                }
            });
            refreshPg = rootView.findViewById(R.id.wifi_refresh_pg_id);
            wifiListView = rootView.findViewById(R.id.wifi_lv_id);
            wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    btnConnectWifi_Click((View) adapter.getItem(position));
                }
            });
            adapter = new DeviceAdapter(wifiListView.getContext());
            wifiListView.setAdapter(adapter);
            isLoad = true;
        }
        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        void onFragmentInteraction(Uri uri);
    }

    //扫描wifi
    private void btnScanWifi_Click(View view) {
        adapter.removeAll(); //清空原来的设备
        adapter.notifyDataSetChanged(); //通知UI更新
        networkHelp.scanWifi();
    }

    //连接wifi
    private void btnConnectWifi_Click(View view) {
        final Wifi wifi;
        if (view != null && (wifi = (Wifi) view.getTag()) != null) {
            final MyAlertInputDialog myAlertInputDialog = new MyAlertInputDialog(rootView.getContext()).builder()
                    .setTitle("请输入")
                    .setEditText("");
            myAlertInputDialog.setPositiveButton("确认", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String password = myAlertInputDialog.getResult();
                    networkHelp.connectWifi(wifi.Ssid, password);
                    myAlertInputDialog.dismiss();
                }
            }).setNegativeButton("取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMsg("取消");
                    myAlertInputDialog.dismiss();
                }
            });
            myAlertInputDialog.show();
        }
    }

    private void showMsg(final String msg) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(rootView.getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    NetworkHelp.OnNetReceiveListener netReceiveListener = new NetworkHelp.OnNetReceiveListener() {
        @Override
        public void onUdpReceiveListener(Gson gson, NetMessage netMessage) {

        }

        @Override
        public void onTcpReceiveListener(Gson gson, NetMessage netMessage) {
            if (!netMessage.MsgStatus || !(netMessage.MsgId == Message.ScanWifi_MsgId || netMessage.MsgId == Message.ConnectWifi_MsgId))
                return;
            try {
                android.os.Message msg = new android.os.Message();
                if (netMessage.MsgId == Message.ScanWifi_MsgId) { //返回wifi列表
                    List<Wifi> wifiList = netMessage.JsonToListObject(gson, Wifi.class);
                    msg.what = SCANWIFI;
                    msg.obj = wifiList;
                }
                if (netMessage.MsgId == Message.ConnectWifi_MsgId) { //返回wifi连接结果
                    msg.what = CONNECTWIFI;
                    msg.obj = netMessage.MsgObj.toString();
                }
                handler.sendMessage(msg); //处理完成后给handler发送消息
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private ImageView.OnClickListener imgOnClickListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View v) {
            Wifi wifi = (Wifi) ((View) v.getParent()).getTag();
            String msg = String.format("Wifi名称：%s\n" +
                            "信号强度：%s\n" +
                            "Mac地址：%s\n",
                    wifi.Ssid,
                    wifi.Rssi,
                    wifi.Mac);
            MessageBox.show(getContext(), msg, "设备信息");
        }
    };
}
