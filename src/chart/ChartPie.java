package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import java.util.Map;

public class ChartPie {

    // Yearly pie chart
    public static JFreeChart createYearlyPieChart(Map<String, Double> categoryTotals, int year) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        return ChartFactory.createPieChart(
                "Yearly Expenses (" + year + ")",
                dataset,
                true,   // legend
                true,   // tooltips
                false   // URLs
        );
    }

    // Monthly pie chart
    public static JFreeChart createMonthlyPieChart(Map<String, Double> categoryTotals, int year, int month) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        return ChartFactory.createPieChart(
                "Monthly Expenses (" + month + "/" + year + ")",
                dataset,
                true,
                true,
                false
        );
    }
}
