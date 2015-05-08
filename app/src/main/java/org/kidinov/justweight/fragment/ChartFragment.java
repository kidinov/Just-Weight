package org.kidinov.justweight.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.db.chart.view.LineChartView;

import org.kidinov.justweight.R;
import org.kidinov.justweight.activity.MainActivity;
import org.kidinov.justweight.util.ChartHelper;
import org.kidinov.justweight.util.DbHelper;
import org.kidinov.justweight.util.EnvUtil;

public class ChartFragment extends BaseFragment {
    private static final String TAG = "ChartFragment";

    private LineChartView chart;
    private ChartHelper chartHelper;
    private CheckBox showAllData;
    private long fromSavedDate;
    private long toSavedDate;

    public static ChartFragment newInstance() {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ChartFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chart, container, false);

        initView(v, savedInstanceState);

        if (savedInstanceState != null) {
            fromSavedDate = savedInstanceState.getLong("from_saved", 0);
            toSavedDate = savedInstanceState.getLong("to_saved", 0);
        }

        chart = (LineChartView) v.findViewById(R.id.chart);
        chartHelper = new ChartHelper((MainActivity) getActivity(), chart);

        showAllData = (CheckBox) v.findViewById(R.id.show_all);

        showAllData.setOnCheckedChangeListener((view, b) -> {
            if (refresh.isEnabled()) {
                if (b) {
                    fromSavedDate = EnvUtil.getLocalFromString(fromDate.getText().toString()).getTime();
                    toSavedDate = EnvUtil.getLocalFromString(toDate.getText().toString()).getTime();
                } else {
                    if (fromSavedDate != 0 && toSavedDate != 0) {
                        fromDate.setText(EnvUtil.getFormattedDate(fromSavedDate));
                        toDate.setText(EnvUtil.getFormattedDate(toSavedDate));
                    }
                }
                setDatesPickerEnabled(!b);
                drawChart();
            } else {
                showAllData.setChecked(!b);
            }
        });

        if (savedInstanceState != null) {
            showAllData.setChecked(savedInstanceState.getBoolean("all_data"));
        }
        setDatesPickerEnabled(!showAllData.isChecked());

        if (savedInstanceState != null) {
            drawChart();
        }

        return v;
    }

    public void drawChart() {
        if (refresh.isEnabled()) {
            refresh.setEnabled(false);
            chart.reset();
            if (!showAllData.isChecked()) {
                chartHelper.redrawGraph(EnvUtil.getLocalFromString(fromDate.getText().toString()), EnvUtil.getLocalFromString(toDate.getText().toString()),
                        this::setRefreshEnabled);
            } else {
                try {
                    long start = DbHelper.getFirstDate();
                    long end = DbHelper.getLastDate();
                    fromDate.setText(EnvUtil.getFormattedDate(start));
                    toDate.setText(EnvUtil.getFormattedDate(end));
                    chartHelper.redrawGraph(EnvUtil.getLocalFromString(fromDate.getText().toString()), EnvUtil.getLocalFromString(toDate.getText().toString()),
                            () -> {
                                setRefreshEnabled();
                                setAllDataChecked(true);
                            });
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                    setAllDataChecked(false);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong("from_saved", fromSavedDate);
        outState.putLong("to_saved", toSavedDate);
        outState.putString("from", fromDate.getText().toString());
        outState.putString("to", toDate.getText().toString());
        outState.putBoolean("all_data", showAllData.isChecked());
        super.onSaveInstanceState(outState);
    }

    private void setRefreshEnabled() {
        if (getActivity() != null && refresh != null) {
            refresh.setEnabled(true);
        }
    }

    private void setAllDataChecked(boolean checked) {
        if (getActivity() != null && showAllData != null) {
            showAllData.setChecked(checked);
        }
    }

    private void setDatesPickerEnabled(boolean b) {
        toDate.setEnabled(b);
        fromDate.setEnabled(b);
    }

    @Override
    public void updateData() {
        drawChart();
    }
}
