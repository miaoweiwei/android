package com.shnuedu.fragmentpage;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.shnuedu.customControl.ItemAdapter;
import com.shnuedu.goodmother.R;
import com.shnuedu.tools.NetUtil;
import com.shnuedu.tools.NetworkHelp;

import java.util.Timer;
import java.util.TimerTask;

public class DeviceFragment extends Fragment {

    private ListView deviceListView;
    private ProgressBar refreshPg;

    private static final String ARG_PARAM1 = "param1";
    private String mParam1;

    private OnFragmentInteractionListener mListener;

    public DeviceFragment() {

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
        View rootView = inflater.inflate(R.layout.fragment_device, container, false);

        refreshPg = (ProgressBar) rootView.findViewById(R.id.refresh_pg_id);
        deviceListView = (ListView) rootView.findViewById(R.id.device_lv_id);

        ItemAdapter adapter = new ItemAdapter(deviceListView.getContext());
        deviceListView.setAdapter(adapter);

        View device1 = inflater.inflate(R.layout.item_device_layout, container, false);
        ((TextView) device1.findViewById(R.id.tv_item_id)).setText("设备一");
        ((ImageView) device1.findViewById(R.id.wifi_iv_id)).setOnClickListener(imgOnClickListener);
        ((ImageView) device1.findViewById(R.id.detail_iv_id)).setOnClickListener(imgOnClickListener);

        View device2 = inflater.inflate(R.layout.item_device_layout, container, false);
        ((TextView) device2.findViewById(R.id.tv_item_id)).setText("设备二");
        ((ImageView) device2.findViewById(R.id.wifi_iv_id)).setOnClickListener(imgOnClickListener);
        ((ImageView) device2.findViewById(R.id.detail_iv_id)).setOnClickListener(imgOnClickListener);

        deviceListView.addFooterView(device1);
        deviceListView.addFooterView(device2);

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

    public interface OnFragmentInteractionListener {
        void onDeviceFragmentInteraction(Uri uri);
    }

    private void TestNet() {
        final NetworkHelp networkHelp = NetworkHelp.getInstance();
        networkHelp.startSearchDevice();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                //TODO 获取搜索到的设备然后去更新UI
            }
        }, 100, 100);

        String ip = NetUtil.getIp();
        String addr = NetUtil.getBroadcastAddr();
        System.out.println(String.format("本机ip为：%s", ip));
        System.out.println(String.format("本机广播地址为：%s", addr));
        Uri uri = Uri.parse("ip://" + ip + "/addr/" + addr);
        onButtonPressed(uri);
    }

    private ImageView.OnClickListener imgOnClickListener = new ImageView.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder messageBox = new AlertDialog.Builder(v.getContext());
            messageBox.setIcon(R.mipmap.ic_launcher_round);
            messageBox.setTitle("标题");
            String str = "";
            switch (v.getId()) {
                case R.id.wifi_iv_id:
                    str = "Wifi";
                    break;
                case R.id.detail_iv_id:
                    str = "详情";
                    break;

            }

            messageBox.setMessage(str);
            messageBox.create();
            messageBox.show();
        }
    };
}
