package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JPanel;

public class ChartLine {

    // Method to create a line chart panel
    public JPanel createLineChart() {
        // Build dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(450, "Food & Beverages", "Jan 2026");
        dataset.addValue(600, "Food & Beverages", "Mac 2026");
        dataset.addValue(100, "Entertainment", "Jan 2026");
        dataset.addValue(130, "Entertainment", "Mac 2026");

        // Create line chart
        JFreeChart lineChart = ChartFactory.createLineChart(
                "Monthly Expenses Trend",   // Chart title
                "Month",                   // X-axis label
                "Amount",                  // Y-axis label
                dataset                    // Data
        );

        // Wrap chart in a panel
        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(780, 550));

        return chartPanel;
    }
}
