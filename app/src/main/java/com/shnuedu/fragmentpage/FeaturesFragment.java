package com.shnuedu.fragmentpage;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.shnuedu.customControl.RemoteControllerView;
import com.shnuedu.goodmother.R;
import com.shnuedu.tools.Device;
import com.shnuedu.tools.ImageUtils;
import com.shnuedu.tools.Message;
import com.shnuedu.tools.NetMessage;
import com.shnuedu.tools.NetworkHelp;

/**
 * 功能页面
 */
public class FeaturesFragment extends Fragment {
    private boolean isLoad = false;

    private View rootView;
    private RemoteControllerView remoteControl = null;
    private TextView mode1Tv = null;
    private TextView mode2Tv = null;
    private TextView mode3Tv = null;
    private TextView mode4Tv = null;

    private TextView frequencyTv = null;
    private TextView strengthTv = null;
    private Handler handler = null;//用于夸线程更新UI
    private static final int UDPRECEIVE = 0;//UDP接收到信息
    private static final int COMMANDRESULT = 1;//TCP接收到信息

    private Button statisticsBtn = null;

    private static final String ARG_PARAM1 = "param1";

    private String mParam1;

    private NetworkHelp networkHelp = NetworkHelp.getInstance();

    private OnFragmentInteractionListener mListener;

    public FeaturesFragment() {
        networkHelp.addOnNetReceiveListener(netReceiveListener);
        handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case UDPRECEIVE:
                        break;
                    case COMMANDRESULT:
                        Device device = (Device) msg.obj;
                        modeSwitch(device.Mode);
                        setfrequencyStrength(device.Strength, device.Frequency, device.Select);
                        setOnOrOff(device.IsBoot);
                        onButtonPressed(String.format("Power#%d", device.PowerBattery));
                        break;
                }
            }
        };
    }

    // TODO: Rename and change types and number of parameters
    public static FeaturesFragment newInstance(String param1) {
        FeaturesFragment fragment = new FeaturesFragment();
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
            rootView = inflater.inflate(R.layout.fragment_features, container, false);
            mode1Tv = rootView.findViewById(R.id.mode1_tv_id);
            mode2Tv = rootView.findViewById(R.id.mode2_tv_id);
            mode3Tv = rootView.findViewById(R.id.mode3_tv_id);
            mode4Tv = rootView.findViewById(R.id.mode4_tv_id);

            frequencyTv = rootView.findViewById(R.id.frequency_tv_id);
            strengthTv = rootView.findViewById(R.id.strength_tv_id);

            statisticsBtn = rootView.findViewById(R.id.statistics_btn_id);

            remoteControl = rootView.findViewById(R.id.re_id);
            DrawRoundMenu(remoteControl);

            isLoad = true;
        }
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        networkHelp.removeOnNetReceiveListener(netReceiveListener);
    }

    int[] images = {R.mipmap.wifi_1, R.mipmap.wifi_2, R.mipmap.wifi_3, R.mipmap.wifi_slash};
    int index = 0;
    ImageButton.OnClickListener imageButtononClickListener = new ImageButton.OnClickListener() {
        @Override
        public void onClick(View v) {
            index++;
            if (index == 4) index = 0;
            v.setBackground(getResources().getDrawable(images[index]));
        }
    };

    public void onButtonPressed(String args) {
        if (mListener != null) {
            mListener.onFeaturesFragmentInteraction(args);
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
        void onFeaturesFragmentInteraction(String args);
    }

    private void DrawRoundMenu(RemoteControllerView roundMenuView) {
        for (int i = 0; i < 4; i++) {//圆盘控件添加4个按钮
            RemoteControllerView.RoundMenu roundMenu = new RemoteControllerView.RoundMenu();
            roundMenu.selectSolidColor = ContextCompat.getColor(getContext(), R.color.gray_9999);
            roundMenu.strokeColor = ContextCompat.getColor(getContext(), R.color.gray_9999);
            roundMenu.icon = ImageUtils.drawable2Bitmap(getActivity(), R.mipmap.feature_next);
            roundMenu.strokeSize = 4;
            roundMenu.tag = i;
            roundMenu.onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (((RemoteControllerView) view).getOnClickIndex()) {
                        case 0:
                            btnDown_Click(view);
                            break;
                        case 1:
                            btnLeft_Click(view);
                            break;
                        case 2:
                            btnUp_Click(view);
                            break;
                        case 3:
                            btnRight_Click(view);
                            break;
                        default:
                    }
                }
            };
            roundMenuView.addRoundMenu(roundMenu);
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnCoreMenu_Click(view);
            }
        };
        roundMenuView.setCoreMenu( //圆中心的按钮
                ContextCompat.getColor(getContext(), R.color.lighterGray),
                ContextCompat.getColor(getActivity(), R.color.gray_9999),
                ContextCompat.getColor(getActivity(), R.color.gray_9999),
                1,
                0.43,
                ImageUtils.drawable2Bitmap(getActivity(), R.mipmap.notboot),
                onClickListener);
    }

    private void btnCoreMenu_Click(View v) {
        networkHelp.sendCommand("core");
    }

    private void btnUp_Click(View v) {
        networkHelp.sendCommand("up");
    }

    private void btnDown_Click(View v) {
        networkHelp.sendCommand("down");
    }

    private void btnLeft_Click(View v) {
        networkHelp.sendCommand("left");
    }

    private void btnRight_Click(View v) {
        networkHelp.sendCommand("right");
    }

    private void modeSwitch(int tag) {
        mode1Tv.setTextColor(mode1Tv.getResources().getColor(android.R.color.black));
        mode2Tv.setTextColor(mode2Tv.getResources().getColor(android.R.color.black));
        mode3Tv.setTextColor(mode3Tv.getResources().getColor(android.R.color.black));
        mode4Tv.setTextColor(mode4Tv.getResources().getColor(android.R.color.black));
        switch (tag) {
            case 0:
                mode1Tv.setTextColor(mode1Tv.getResources().getColor(android.R.color.holo_red_light));
                break;
            case 1:
                mode2Tv.setTextColor(mode2Tv.getResources().getColor(android.R.color.holo_red_light));
                break;
            case 2:
                mode3Tv.setTextColor(mode3Tv.getResources().getColor(android.R.color.holo_red_light));
                break;
            case 3:
                mode4Tv.setTextColor(mode4Tv.getResources().getColor(android.R.color.holo_red_light));
                break;
        }
    }

    private void setfrequencyStrength(int strength, int frequency, String activate) {
        frequencyTv.setTextColor(frequencyTv.getResources().getColor(android.R.color.black));
        strengthTv.setTextColor(strengthTv.getResources().getColor(android.R.color.black));
        frequencyTv.setText("频率：" + frequency);
        strengthTv.setText("强度：" + strength);
        if (activate.equals("Frequency")) { //当前是频率模式
            frequencyTv.setTextColor(frequencyTv.getResources().getColor(android.R.color.holo_red_light));
        } else if (activate.equals("Strength")) { //当前是强度模式
            strengthTv.setTextColor(strengthTv.getResources().getColor(android.R.color.holo_red_light));
        }
    }

    private void setOnOrOff(boolean tag) {
        if (tag) //开机状态
            remoteControl.setCoreBitmap(ImageUtils.drawable2Bitmap(getActivity(), R.mipmap.boot));
        else //关机状态
            remoteControl.setCoreBitmap(ImageUtils.drawable2Bitmap(getActivity(), R.mipmap.notboot));
    }

    NetworkHelp.OnNetReceiveListener netReceiveListener = new NetworkHelp.OnNetReceiveListener() {
        @Override
        public void onUdpReceiveListener(Gson gson, NetMessage netMessage) {

        }

        @Override
        public void onTcpReceiveListener(Gson gson, NetMessage netMessage) {
            if (netMessage.MsgId != Message.Command_MsgId || !netMessage.MsgStatus) return;
            try {
                Device device = netMessage.JsonToObject(gson, Device.class);
                if (device == null) return;

                android.os.Message msg = new android.os.Message();
                msg.what = COMMANDRESULT;
                msg.obj = device;
                handler.sendMessage(msg); //告诉主线程去更新UI
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
