package com.shnuedu.fragmentpage;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.shnuedu.customControl.ItemAdapter;
import com.shnuedu.goodmother.R;

public class SettingFragment extends Fragment {
    private ListView settingListView;
    private String[] setArr = {"吸乳器", "使用说明", "常见问题"};

    private static final String ARG_SHOW_TEXT = "param1";
    private String mContentText;

    private OnFragmentInteractionListener mListener;

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance(String param1) {
        SettingFragment fragment = new SettingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SHOW_TEXT, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mContentText = getArguments().getString(ARG_SHOW_TEXT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        settingListView = rootView.findViewById(R.id.setting_list_id);

        ItemAdapter adapter;
        adapter = new ItemAdapter(settingListView.getContext());
//        adapter.addItem(setArr[0]);
//        adapter.addItem(setArr[1]);
        settingListView.setAdapter(adapter);

        View item_switch_breastPump = inflater.inflate(R.layout.item_switch_layout, container, false);
        TextView textView = (TextView) item_switch_breastPump.findViewById(R.id.tv_item_id);
        textView.setText(setArr[0]);
        ((Switch) item_switch_breastPump.findViewById(R.id.switch_item_id)).setText("设备一");

        View item_arrow_useDescription = inflater.inflate(R.layout.item_arrow_layout, container, false);
        ((TextView) item_arrow_useDescription.findViewById(R.id.tv_item_id)).setText(setArr[1]);

        View item_arrow_commonProblem = inflater.inflate(R.layout.item_arrow_layout, container, false);
        ((TextView) item_arrow_commonProblem.findViewById(R.id.tv_item_id)).setText(setArr[2]);

        settingListView.addFooterView(item_switch_breastPump);
        settingListView.addFooterView(item_arrow_useDescription);
        settingListView.addFooterView(item_arrow_commonProblem);

        settingListView.setOnItemClickListener(steListViewItemClickListener);
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String args) {
        if (mListener != null) {
            mListener.onSettingFragmentInteraction(args);
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
        void onSettingFragmentInteraction(String args);
    }


    ListView.OnItemClickListener steListViewItemClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String temp = ((TextView) view.findViewById(R.id.tv_item_id)).getText().toString();
            onButtonPressed(temp);
        }
    };
}
