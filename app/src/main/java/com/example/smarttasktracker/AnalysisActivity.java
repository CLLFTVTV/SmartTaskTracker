package com.example.smarttasktracker;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class AnalysisActivity extends AppCompatActivity {

    DatabaseHelper db;
    PieChart pieChart;
    BarChart barChart;
    BarChart barChartDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        db = new DatabaseHelper(this);
        pieChart = findViewById(R.id.pieChart);
        barChart = findViewById(R.id.barChart);
        barChartDuration = findViewById(R.id.barChartDuration);

        loadPieChart();
        loadCostChart();
        loadDurationChart();
    }

    private void loadPieChart() {
        List<Task> activeTasks = db.getTasks(false, 0);
        List<Task> completedTasks = db.getTasks(true, 0);

        List<PieEntry> entries = new ArrayList<>();
        if (!activeTasks.isEmpty()) entries.add(new PieEntry(activeTasks.size(), "Active"));
        if (!completedTasks.isEmpty())
            entries.add(new PieEntry(completedTasks.size(), "Completed"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // VISUAL POLISH: Make it a Donut Chart
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false); // Hide legend if labels are clear
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void loadCostChart() {
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(db.getTasks(false, 0));
        allTasks.addAll(db.getTasks(true, 0));

        if (allTasks.isEmpty()) return;

        List<BarEntry> entries = new ArrayList<>();
        final ArrayList<String> taskNames = new ArrayList<>();

        for (int i = 0; i < allTasks.size(); i++) {
            entries.add(new BarEntry(i, (float) allTasks.get(i).getCost()));
            taskNames.add(allTasks.get(i).getName());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Cost");
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f); // Make bars slightly slimmer

        barChart.setData(data);
        setupBarChartStyle(barChart, taskNames); // Apply clean style
    }

    private void loadDurationChart() {
        List<Task> tasks = db.getTasks(true, 0);

        if (tasks.isEmpty()) {
            barChartDuration.clear();
            return;
        }

        List<BarEntry> expectedEntries = new ArrayList<>();
        List<BarEntry> actualEntries = new ArrayList<>();
        final ArrayList<String> taskNames = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            expectedEntries.add(new BarEntry(i, t.getExpectedMinutes()));

            float actualMinutes = t.getActualSeconds() / 60f;
            actualEntries.add(new BarEntry(i, actualMinutes));

            taskNames.add(t.getName());
        }

        BarDataSet set1 = new BarDataSet(expectedEntries, "Expected");
        set1.setColor(Color.parseColor("#FFC107")); // Amber
        set1.setValueTextSize(10f);

        BarDataSet set2 = new BarDataSet(actualEntries, "Actual");
        set2.setColor(Color.parseColor("#4CAF50")); // Green
        set2.setValueTextSize(10f);

        float groupSpace = 0.4f;
        float barSpace = 0.05f;
        float barWidth = 0.25f;

        BarData data = new BarData(set1, set2);
        data.setBarWidth(barWidth);

        barChartDuration.setData(data);

        // Special Group setup
        barChartDuration.getXAxis().setAxisMinimum(0);
        barChartDuration.getXAxis().setAxisMaximum(0 + barChartDuration.getBarData().getGroupWidth(groupSpace, barSpace) * tasks.size());
        barChartDuration.groupBars(0, groupSpace, barSpace);

        setupBarChartStyle(barChartDuration, taskNames); // Apply clean style
        barChartDuration.getXAxis().setCenterAxisLabels(true); // Keep this for grouping
    }

    // --- HELPER TO CLEAN UP CHARTS ---
    private void setupBarChartStyle(BarChart chart, List<String> labels) {
        // 1. Disable Right Axis (The User's Request)
        chart.getAxisRight().setEnabled(false);

        // 2. Clean Left Axis
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false); // Remove grid lines
        leftAxis.setAxisMinimum(0f); // Start at 0

        // 3. Clean X Axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false); // Remove vertical grid lines
        xAxis.setLabelRotationAngle(-20); // Slight tilt is better than -45

        // 4. General Cleanup
        chart.getDescription().setEnabled(false);
        chart.setFitBars(true);
        chart.animateY(1000);
        chart.invalidate();
    }
}