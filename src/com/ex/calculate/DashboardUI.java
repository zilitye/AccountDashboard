package com.ex.calculate;
import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class DashboardUI extends JFrame {

    public DashboardUI() {
        setTitle("Account Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Example dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(450, "Food & Beverages", "Jan 2026");
        dataset.addValue(100, "Entertainment", "Jan 2026");
        dataset.addValue(600, "Food & Beverages", "Mac 2026");
        dataset.addValue(130, "Entertainment", "Mac 2026");

        // Create bar chart
        JFreeChart barChart = ChartFactory.createBarChart(
                "Monthly Expenses",
                "Category",
                "Amount",
                dataset
        );

        // Wrap chart in panel
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(780, 550));
        setContentPane(chartPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DashboardUI ui = new DashboardUI();
            ui.setVisible(true);
        });
    }
}
