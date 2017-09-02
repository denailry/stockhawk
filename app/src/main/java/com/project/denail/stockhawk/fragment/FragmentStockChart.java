package com.project.denail.stockhawk.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.project.denail.stockhawk.R;
import com.project.denail.stockhawk.data.DataStock_Table;
import com.project.denail.stockhawk.data.DataStock;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.project.denail.stockhawk.MainActivity.KEY_TITLE;

/**
 * Created by denail on 17/08/26.
 */

public class FragmentStockChart extends Fragment {

    @BindView(R.id.chart_fragment_stockchart) LineChart chart;

    private String currentDataTitle;

    public static FragmentStockChart newInstance(String dataTitle) {
        FragmentStockChart fragment = new FragmentStockChart();

        Bundle args = new Bundle();
        args.putString(KEY_TITLE, dataTitle);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stockchart, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle args = getArguments();
        if(args != null) {
            if(args.containsKey(KEY_TITLE)) {
                currentDataTitle = args.getString(KEY_TITLE);
            }
        }
        setData(currentDataTitle);
    }

    private void drawChart(DataStock data) {
        List<Entry> entryList = new ArrayList<>();

        ArrayList<Float> listValue = data.getValues();
        for(int i = listValue.size()-1; i >= 0; --i) {
            float dataX = data.getLastDate()-i;
            float dataY = listValue.get(i);
            entryList.add(new Entry(dataX, dataY));
        }

        LineDataSet lineDataSet = new LineDataSet(entryList, data.getTitle());
        lineDataSet.setFillColor(Color.parseColor("#0055aa"));
        lineDataSet.setDrawFilled(true);
        lineDataSet.setHighLightColor(Color.parseColor("#ff0000"));
        LineData lineData = new LineData(lineDataSet);
        XAxis xAxis = chart.getXAxis();
        YAxis yLeftAxis = chart.getAxisLeft();
        YAxis yRightAxis = chart.getAxisRight();
        xAxis.setDrawGridLines(false);
        yLeftAxis.setDrawGridLines(false);
        yRightAxis.setDrawGridLines(false);
        chart.setDescription(null);
        chart.setContentDescription(null);
        chart.setData(lineData);
        chart.invalidate();
    }

    public void setData(String dataTitle) {
        if(chart != null && dataTitle != null) {
            DataStock data = new Select()
                    .from(DataStock.class)
                    .where(DataStock_Table.title.eq(dataTitle))
                    .querySingle();
            if(data != null) {
                drawChart(data);
            }
        }
    }
}
