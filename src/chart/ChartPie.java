package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Modern Pie Chart with enhanced styling and better visual hierarchy
 * Features: Modern colors, improved labels, better legend placement
 */
public class ChartPie {
    
    // Modern Color Palette for pie slices
    private static final Color[] MODERN_COLORS = {
        new Color(255, 99, 132),    // Blue
        new Color(54, 162, 235),    // Green
        new Color(255, 206, 86),    // Amber
        new Color(75, 192, 192),     // Red
        new Color(153, 102, 255),    // Purple
        new Color(255, 159, 64),    // Sky Blue
        new Color(255, 87, 51),    // Pink
        new Color(199, 0, 57),      // Emerald
        new Color(144, 12, 63),      // Emerald
        new Color(88, 24, 69)      // Emerald
    };

    /**
     * Create yearly pie chart showing expense distribution by category
     * @param categoryTotals map of category to total expense
     * @param year the year displayed
     * @return styled JFreeChart
     */
    public static JFreeChart createYearlyPieChart(Map<String, Double> categoryTotals, int year) {
        DefaultPieDataset dataset = createDataset(categoryTotals);
        
        JFreeChart chart = ChartFactory.createPieChart(
                "Yearly Expenses by Category - " + year,
                dataset,
                true,   // legend
                true,   // tooltips
                false   // URLs
        );
        
        stylePieChart(chart);
        return chart;
    }

    /**
     * Create monthly pie chart showing expense distribution by category
     * @param categoryTotals map of category to total expense
     * @param year the year
     * @param month the month
     * @return styled JFreeChart
     */
    public static JFreeChart createMonthlyPieChart(Map<String, Double> categoryTotals, int year, int month) {
        DefaultPieDataset dataset = createDataset(categoryTotals);
        
        String monthName = getMonthName(month);
        JFreeChart chart = ChartFactory.createPieChart(
                "Monthly Expenses by Category - " + monthName + " " + year,
                dataset,
                true,
                true,
                false
        );
        
        stylePieChart(chart);
        return chart;
    }

    /**
     * Create dataset from category totals
     */
    private static DefaultPieDataset createDataset(Map<String, Double> categoryTotals) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }
        return dataset;
    }

    /**
     * Apply modern styling to the pie chart
     */
    private static void stylePieChart(JFreeChart chart) {
        PiePlot plot = (PiePlot) chart.getPlot();
        
        // Apply modern colors to pie slices
        int colorIndex = 0;
        for (Object key : plot.getDataset().getKeys()) {
            Color color = MODERN_COLORS[colorIndex % MODERN_COLORS.length];
            plot.setSectionPaint((Comparable<?>) key, color);
            colorIndex++;
        }
        
        // Styling properties
        plot.setBackgroundPaint(null);
        plot.setOutlineVisible(false);
        
        // Label properties
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.setLabelPaint(new Color(17, 24, 39));
        // Remove label background, shadow, and outline
        plot.setLabelBackgroundPaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelOutlineStroke(null);
        
        // Legend properties
        chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        // Title properties
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
        chart.getTitle().setPaint(new Color(17, 24, 39));
    }

    /**
     * Helper method to convert month number to month name
     */
    private static String getMonthName(int month) {
        String[] months = {"", "January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        return (month >= 1 && month <= 12) ? months[month] : "Month " + month;
    }
}
