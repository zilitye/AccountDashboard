package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.Map;

public class ChartBar {

    // Bar chart for category breakdown (monthly or yearly)
    public static JFreeChart createCategoryBarChart(Map<String, Double> categoryTotals, String title, String xLabel, String yLabel) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            dataset.addValue(entry.getValue(), "Expenses", entry.getKey());
        }

        return ChartFactory.createBarChart(
                title,      // Chart title
                xLabel,     // X-axis label
                yLabel,     // Y-axis label
                dataset     // Dataset
        );
    }

    // Bar chart for month-to-month comparison
    public static JFreeChart createMonthComparisonChart(double month1Total, double month2Total, int month1, int month2, int year) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(month1Total, "Expenses", "Month " + month1);
        dataset.addValue(month2Total, "Expenses", "Month " + month2);

        return ChartFactory.createBarChart(
                "Comparison of " + month1 + " vs " + month2 + " (" + year + ")",
                "Month",
                "Amount",
                dataset
        );
    }

    // General bar chart for yearly totals (optional extension)
    public static JFreeChart createYearlyBarChart(Map<Integer, Double> yearlyTotals) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Double> entry : yearlyTotals.entrySet()) {
            dataset.addValue(entry.getValue(), "Expenses", String.valueOf(entry.getKey()));
        }

        return ChartFactory.createBarChart(
                "Yearly Expenses",
                "Year",
                "Amount",
                dataset
        );
    }
}
