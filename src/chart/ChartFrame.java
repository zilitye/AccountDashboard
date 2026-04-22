package chart;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern ChartFrame with enhanced UI styling
 * Features: Modern color scheme, typography, spacing, and interactivity
 */
public class ChartFrame extends JPanel {
    private ChartPanel chartPanel;
    
    // Modern Color Palette
    private static final Color COLOR_BACKGROUND = new Color(248, 249, 250);      // Light gray
    private static final Color COLOR_PANEL_BG = new Color(255, 255, 255);         // Pure white
    private static final Color COLOR_TEXT_PRIMARY = new Color(17, 24, 39);        // Dark charcoal
    private static final Color COLOR_TEXT_SECONDARY = new Color(75, 85, 99);      // Medium gray
    private static final Color COLOR_BORDER = new Color(229, 231, 235);           // Light border
    //private static final Color COLOR_ACCENT = new Color(59, 130, 246);            // Modern blue

    // Fonts
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 11);

    public ChartFrame(JFreeChart chart) {
        initializeFrame(chart);
    }

    private void initializeFrame(JFreeChart chart) {
        setLayout(new BorderLayout());
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        // Configure chart appearance with modern styling
        styleChart(chart);
        
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 550));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setMouseZoomable(true);
        chartPanel.setRangeZoomable(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setBackground(COLOR_PANEL_BG);
        
        // No border for the chart panel
        chartPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        
        add(chartPanel, BorderLayout.CENTER);
    }

    /**
     * Apply modern styling to the chart
     */
    private void styleChart(JFreeChart chart) {
        // Chart background
        chart.setBackgroundPaint(COLOR_PANEL_BG);
        
        // Title styling
        if (chart.getTitle() != null) {
            chart.getTitle().setFont(FONT_TITLE);
            chart.getTitle().setPaint(COLOR_TEXT_PRIMARY);
            chart.getTitle().setMargin(0, 0, 8, 0);
        }
        
        // Plot background
        if (chart.getPlot() != null) {
            //chart.getPlot().setBackgroundPaint(COLOR_BACKGROUND);
            chart.getPlot().setOutlineVisible(false);
            
            // Configure grid lines for better readability
            if (chart.getPlot() instanceof org.jfree.chart.plot.CategoryPlot) {
                org.jfree.chart.plot.CategoryPlot plot = (org.jfree.chart.plot.CategoryPlot) chart.getPlot();
                plot.setRangeGridlinePaint(COLOR_BORDER);
                plot.setRangeGridlineStroke(new BasicStroke(0.5f));
                plot.getDomainAxis().setLabelFont(FONT_LABEL);
                plot.getDomainAxis().setTickLabelFont(FONT_LABEL);
                plot.getRangeAxis().setLabelFont(FONT_LABEL);
                plot.getRangeAxis().setTickLabelFont(FONT_LABEL);
                plot.getDomainAxis().setLabelPaint(COLOR_TEXT_SECONDARY);
                plot.getRangeAxis().setLabelPaint(COLOR_TEXT_SECONDARY);
            } else if (chart.getPlot() instanceof org.jfree.chart.plot.PiePlot) {
                org.jfree.chart.plot.PiePlot plot = (org.jfree.chart.plot.PiePlot) chart.getPlot();
                plot.setLabelFont(FONT_LABEL);
            }
        }
    }

    /**
     * Update chart dynamically with modern styling
     */
    public void setChart(JFreeChart chart) {
        if (chartPanel != null) {
            remove(chartPanel);
        }
        
        styleChart(chart);
        
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 550));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setMouseZoomable(true);
        chartPanel.setRangeZoomable(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setBackground(COLOR_PANEL_BG);
        
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_BORDER, 1),
            new EmptyBorder(8, 8, 8, 8)
        ));
        
        add(chartPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Get the chart panel for advanced customization
     */
    public ChartPanel getChartPanel() {
        return chartPanel;
    }
}
