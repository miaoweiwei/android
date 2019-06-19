package com.shnuedu.fragmentpage;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.shnuedu.customControl.RemoteControllerView;
import com.shnuedu.goodmother.R;
import com.shnuedu.tools.Device;
import com.shnuedu.tools.ImageUtils;
import com.shnuedu.tools.NetworkHelp;

/**
 * 功能页面
 */
public class FeaturesFragment extends Fragment implements NetworkHelp.OnNetReceiveListener {
    private boolean isLoad = false;


    private View rootView;
    private RemoteControllerView remoteControl = null;
    private TextView textView = null;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private NetworkHelp networkHelp = NetworkHelp.getInstance();

    private OnFragmentInteractionListener mListener;

    public FeaturesFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static FeaturesFragment newInstance(String param1, String param2) {
        FeaturesFragment fragment = new FeaturesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!isLoad) {
            rootView = inflater.inflate(R.layout.fragment_features, container, false);
            textView = rootView.findViewById(R.id.moshi_tv_id);
            remoteControl = rootView.findViewById(R.id.re_id);
            DrawRoundMenu(remoteControl);
            isLoad = true;
        }
        return rootView;
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

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFeaturesFragmentInteraction(uri);
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
        void onFeaturesFragmentInteraction(Uri uri);
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
        remoteControl.setCoreBitmap(ImageUtils.drawable2Bitmap(getActivity(), R.mipmap.boot));
        //networkHelp.sendMessageToDevice("zpf1000 \n");
    }

    private void btnUp_Click(View v) {
        networkHelp.sendMessageToDevice("zpf1000 \n");
    }

    private void btnDown_Click(View v) {
        networkHelp.sendMessageToDevice("zpf0100 \n");
    }

    private void btnLeft_Click(View v) {
        networkHelp.sendMessageToDevice("zpf0010 \n");
    }

    private void btnRight_Click(View v) {
        networkHelp.sendMessageToDevice("zpf0001 \n");
    }

    @Override
    public void onUdpReceiveListener(Device device) {

    }

    @Override
    public void onTcpReceiveListener(String msg) {
        textView.setText(msg);
        System.out.println("TCp:" + msg);
    }
}
