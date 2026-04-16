package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JPanel;

public class ChartBar {

    // Method to create a bar chart panel
    public JPanel createBarChart() {
        // Build dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(450, "Food & Beverages", "Jan 2026");
        dataset.addValue(100, "Entertainment", "Jan 2026");
        dataset.addValue(600, "Food & Beverages", "Mac 2026");
        dataset.addValue(130, "Entertainment", "Mac 2026");

        // Create chart
        JFreeChart barChart = ChartFactory.createBarChart(
                "Monthly Expenses",   // Chart title
                "Category",           // X-axis label
                "Amount",             // Y-axis label
                dataset               // Data
        );

        // Wrap chart in a panel
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(780, 550));

        return chartPanel;
    }
}
