package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import java.util.Map;

public class ChartPie {
    public static JFreeChart createYearlyPieChart(Map<String, Double> data, int year) {
        return createChart("Yearly Expenses (" + year + ")", data);
    }

    public static JFreeChart createMonthlyPieChart(Map<String, Double> data, int year, int month) {
        return createChart("Monthly Expenses (" + month + "/" + year + ")", data);
    }

    // Consolidated repetitive logic into one clean helper method
    private static JFreeChart createChart(String title, Map<String, Double> data) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        data.forEach(dataset::setValue); // One-line population
        return ChartFactory.createPieChart(title, dataset, true, true, false);
    }
}