package com.cs427.project.mytagcloud;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GraphActivity extends AppCompatActivity {

    private LineChart chart;
    private List<ILineDataSet> dataSets;
    private static final String[] months = new String[] {"Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct"};
    private static final Integer[] colors = new Integer[] {Color.parseColor("#1f78b4"), Color.parseColor("#33a02c"), Color.parseColor("#e31a1c"), Color.parseColor("#ff7f00"),
            Color.parseColor("#6a3d9a"), Color.parseColor("#b15928"), Color.parseColor("#a6cee3"), Color.parseColor("#b2df8a"), Color.parseColor("#fb9a99"), Color.parseColor("#fdbf6f"),
            Color.parseColor("#cab2d6"), Color.parseColor("#ffff99"), Color.parseColor("#ffffb3"), Color.parseColor("#bebada"), Color.parseColor("#8dd3c7"), Color.parseColor("#bc80bd"),
            Color.parseColor("#fccde5"), Color.parseColor("#d95f02"), Color.parseColor("#e7298a"), Color.parseColor("#386cb0")};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        chart = (LineChart) findViewById(R.id.chart);
        dataSets = new ArrayList<ILineDataSet>();
        initGraph();
    }

    public void initGraph() {
        HashMap<String, List<Integer>> data;
        if(TagCloudApp.getInstance().isTwitter) {
            data = TagCloudApp.getInstance().twitterTimeSeries;
        }
        else {
            data = TagCloudApp.getInstance().facebookTimeSeries;
        }

        int colorIndex = 0;
        for(String pageName : TagCloudApp.getInstance().selectedItems) {
            //Log.d("GRAPH", pageName);
            List<Entry> entries = new ArrayList<Entry>();
            for(int i = 0; i < data.get(pageName).size(); ++i) {
                entries.add(new Entry((float)i, data.get(pageName).get(i)));
            }

            LineDataSet lineDataSet= new LineDataSet(entries, pageName);
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineDataSet.setColor(colors[colorIndex]);
            dataSets.add(lineDataSet);
            ++colorIndex;
        }

        LineData lineData = new LineData(dataSets);
        lineData.setValueFormatter(new LargeValueFormatter());
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new MyAxisFormatter());

        YAxis left = chart.getAxisLeft();
        left.setDrawGridLines(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setForm(Legend.LegendForm.LINE);
        legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_CENTER);

        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);
        //chart.setPinchZoom(true);
        chart.getAxisRight().setEnabled(false);
        chart.setBackgroundColor(Color.WHITE);

        chart.setDescription("Fan count over the last 6 months");

        chart.setData(lineData);
        chart.animateXY(1500, 1500);
        //chart.invalidate();
    }

    public class MyAxisFormatter implements AxisValueFormatter {
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return months[(int) value];
        }

        // we don't draw numbers, so no decimal digits needed
        @Override
        public int getDecimalDigits() {  return 0; }

    }


    @Override
    public void onBackPressed()
    {
        Intent mStartActivity = new Intent(this, TagCloudActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }
}
