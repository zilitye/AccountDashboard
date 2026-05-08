package chart;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;

public class ChartFrame extends JPanel {

    public ChartFrame(JFreeChart chart) {
        setLayout(new BorderLayout());
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 500));
        chartPanel.setMouseWheelEnabled(true); // Allow zooming
        
        add(chartPanel, BorderLayout.CENTER);
    }
}