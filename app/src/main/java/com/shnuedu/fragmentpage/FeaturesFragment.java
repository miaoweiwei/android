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

import com.shnuedu.customControl.RoundProgress;
import com.shnuedu.customControl.RemoteControllerView;
import com.shnuedu.goodmother.R;
import com.shnuedu.tools.ImageUtils;
import com.shnuedu.tools.ToastUtils;

/**
 * 功能页面
 */
public class FeaturesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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
        View rootView = inflater.inflate(R.layout.fragment_features, container, false);

        RoundProgress roundProgress = rootView.findViewById(R.id.my_id);
        roundProgress.setSweepValue(68f);
        roundProgress.setShowTextSize(40f);

        RemoteControllerView remoteControllerView = rootView.findViewById(R.id.re_id);
        DrawRoundMenu(remoteControllerView);

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
        for (int i = 0; i < 5; i++) {
            RemoteControllerView.RoundMenu roundMenu = new RemoteControllerView.RoundMenu();
            roundMenu.selectSolidColor = ContextCompat.getColor(getContext(), R.color.gray_9999);
            roundMenu.strokeColor = ContextCompat.getColor(getContext(), R.color.gray_9999);
            roundMenu.icon = ImageUtils.drawable2Bitmap(getActivity(), R.mipmap.features_next);
            roundMenu.strokeSize = 2;
            roundMenu.tag = i;
            roundMenu.onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ToastUtils.showToast(getActivity(), "点击了" + ((RemoteControllerView) view).getOnClickIndex());
                }
            };
            roundMenuView.addRoundMenu(roundMenu);
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtils.showToast(getActivity(), "点击了中心圆圈");
            }
        };
        roundMenuView.setCoreMenu(
                ContextCompat.getColor(getActivity(), R.color.gray_f2f2),
                ContextCompat.getColor(getActivity(), R.color.gray_9999),
                ContextCompat.getColor(getActivity(), R.color.gray_9999),
                1,
                0.43,
                ImageUtils.drawable2Bitmap(getActivity(), R.mipmap.ok),
                onClickListener);
    }
}
