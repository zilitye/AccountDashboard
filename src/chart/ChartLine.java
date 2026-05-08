package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import java.util.Map;

public class ChartLine {
    public static JFreeChart createMonthlyTrendChart(Map<Integer, Double> data, int year) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        data.forEach((k, v) -> dataset.addValue(v, "Expenses", "Month " + k));
        return ChartFactory.createLineChart("Monthly Trend (" + year + ")", "Month", "Amount", dataset);
    }
}