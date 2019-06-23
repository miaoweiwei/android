package com.shnuedu.fragmentpage;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.shnuedu.goodmother.R;

import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {

    private boolean isLoad = false;

    private View rootView;

    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private OnFragmentInteractionListener mListener;

    public StatisticsFragment() {
    }

    public static StatisticsFragment newInstance(String param1) {
        StatisticsFragment fragment = new StatisticsFragment();
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
            rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

            //region 折线图
            LineChart lineChart = (LineChart) rootView.findViewById(R.id.line_char_id);
            List<Entry> valsComp1 = new ArrayList<Entry>();
            Entry c1e1 = new Entry(0f, 100000f); // 0 == quarter 1
            valsComp1.add(c1e1);
            Entry c1e2 = new Entry(1f, 140000f); // 1 == quarter 2 ...
            valsComp1.add(c1e2);
            LineDataSet setComp1 = new LineDataSet(valsComp1, "Company 1");
            setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
            List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(setComp1);
            LineData data = new LineData(dataSets);
            lineChart.setData(data);
            lineChart.invalidate(); // refresh
            //endregion

            //region 柱状图
            List<BarEntry> valsCompb1 = new ArrayList<BarEntry>();
            BarEntry c1eb1 = new BarEntry(0f, 10); // 0 == quarter 1
            valsCompb1.add(c1eb1);
            BarEntry c1eb2 = new BarEntry(1f, 14); // 1 == quarter 2 ...
            valsCompb1.add(c1eb2);
            BarEntry c1eb3 = new BarEntry(2f, 20); // 1 == quarter 2 ...
            valsCompb1.add(c1eb3);

            BarDataSet setCompb1 = new BarDataSet(valsCompb1, "Company barchar");

//        setCompb1.setAxisDependency(YAxis.AxisDependency.LEFT);

            BarChart barChart = (BarChart) rootView.findViewById(R.id.bar_chart_id);
            BarData barData = new BarData(setCompb1);
            barChart.setData(barData);
            barChart.invalidate(); // refresh
            //endregion

            isLoad = true;
        }
        return rootView;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onStatisticFragmentInteraction(uri);
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
        // TODO: Update argument type and name
        void onStatisticFragmentInteraction(Uri uri);
    }
}
