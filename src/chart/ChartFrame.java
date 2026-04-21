package chart;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;

public class ChartFrame extends JPanel {
    private ChartPanel chartPanel;
    private static final Color COLOR_BACKGROUND = new Color(255, 255, 255);
    private static final Color COLOR_TEXT = new Color(17, 24, 39);

    // Constructor accepts a JFreeChart object
    public ChartFrame(JFreeChart chart) {
        setLayout(new BorderLayout());
        setBackground(COLOR_BACKGROUND);
        
        // Configure chart appearance for dark theme
        chart.setBackgroundPaint(COLOR_BACKGROUND);
        chart.getPlot().setBackgroundPaint(COLOR_BACKGROUND);
        chart.getTitle().setPaint(COLOR_TEXT);
        
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 500));
        chartPanel.setMouseWheelEnabled(true); // zoom with mouse wheel
        chartPanel.setBackground(COLOR_BACKGROUND);
        add(chartPanel, BorderLayout.CENTER);
    }

    // Method to update chart dynamically
    public void setChart(JFreeChart chart) {
        remove(chartPanel);
        chart.setBackgroundPaint(COLOR_BACKGROUND);
        chart.getPlot().setBackgroundPaint(COLOR_BACKGROUND);
        chart.getTitle().setPaint(COLOR_TEXT);
        
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 500));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setBackground(COLOR_BACKGROUND);
        add(chartPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // Optional: expose the chart panel for customization
    public ChartPanel getChartPanel() {
        return chartPanel;
    }
}
