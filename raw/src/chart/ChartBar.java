package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import java.util.Map;

public class ChartBar {
    public static JFreeChart createCategoryBarChart(Map<String, Double> data, String title, String x, String y) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        data.forEach((k, v) -> dataset.addValue(v, "Expenses", k));
        return ChartFactory.createBarChart(title, x, y, dataset);
    }
}