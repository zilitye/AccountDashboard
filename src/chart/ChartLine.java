package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.Map;

public class ChartLine {

    // Line chart for monthly expenses trend in a given year
    public static JFreeChart createMonthlyTrendChart(Map<Integer, Double> monthlyTotals, int year) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Double> entry : monthlyTotals.entrySet()) {
            dataset.addValue(entry.getValue(), "Expenses", "Month " + entry.getKey());
        }

        return ChartFactory.createLineChart(
                "Monthly Expenses Trend (" + year + ")",
                "Month",
                "Amount",
                dataset
        );
    }

    // Line chart for yearly expenses trend across multiple years
    public static JFreeChart createYearlyTrendChart(Map<Integer, Double> yearlyTotals) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Double> entry : yearlyTotals.entrySet()) {
            dataset.addValue(entry.getValue(), "Expenses", String.valueOf(entry.getKey()));
        }

        return ChartFactory.createLineChart(
                "Yearly Expenses Trend",
                "Year",
                "Amount",
                dataset
        );
    }

    // Line chart for category-specific monthly trend
    public static JFreeChart createCategoryTrendChart(Map<Integer, Double> monthlyCategoryTotals, int year, String category) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Double> entry : monthlyCategoryTotals.entrySet()) {
            dataset.addValue(entry.getValue(), category, "Month " + entry.getKey());
        }

        return ChartFactory.createLineChart(
                "Monthly Trend for " + category + " (" + year + ")",
                "Month",
                "Amount",
                dataset
        );
    }
}
