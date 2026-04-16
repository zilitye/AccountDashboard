package chart;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;

public class ChartFrame extends JPanel {
    private ChartPanel chartPanel;

    // Constructor accepts a JFreeChart object
    public ChartFrame(JFreeChart chart) {
        setLayout(new BorderLayout());
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 500));
        chartPanel.setMouseWheelEnabled(true); // zoom with mouse wheel
        add(chartPanel, BorderLayout.CENTER);
    }

    // Method to update chart dynamically
    public void setChart(JFreeChart chart) {
        remove(chartPanel);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 500));
        chartPanel.setMouseWheelEnabled(true);
        add(chartPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // Optional: expose the chart panel for customization
    public ChartPanel getChartPanel() {
        return chartPanel;
    }
}
