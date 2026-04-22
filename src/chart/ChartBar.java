package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.util.Map;

/**
 * Modern Bar Chart with enhanced styling and visual hierarchy
 * Features: Modern colors, improved axis labels, better legend formatting
 */
public class ChartBar {
    
    // Modern color for bars
    private static final Color BAR_COLOR = new Color(59, 130, 246);      // Modern blue
    private static final Color BAR_COLOR_SECONDARY = new Color(16, 185, 129); // Modern green
    private static final Color AXIS_COLOR = new Color(75, 85, 99);       // Medium gray
    private static final Color GRID_COLOR = new Color(229, 231, 235);    // Light border

    /**
     * Create category bar chart showing expense breakdown
     * @param categoryTotals map of category to amount
     * @param title chart title
     * @param xLabel x-axis label
     * @param yLabel y-axis label
     * @return styled JFreeChart
     */
    public static JFreeChart createCategoryBarChart(Map<String, Double> categoryTotals, 
                                                    String title, String xLabel, String yLabel) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            dataset.addValue(entry.getValue(), "Expenses", entry.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title,
                xLabel,
                yLabel,
                dataset
        );

        styleBarChart(chart, false);
        return chart;
    }

    /**
     * Create month-to-month comparison bar chart
     * @param month1Total expenses for first month
     * @param month2Total expenses for second month
     * @param month1 first month number
     * @param month2 second month number
     * @param year the year
     * @return styled JFreeChart
     */
    public static JFreeChart createMonthComparisonChart(double month1Total, double month2Total, 
                                                        int month1, int month2, int year) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String month1Name = getMonthName(month1);
        String month2Name = getMonthName(month2);
        
        dataset.addValue(month1Total, "Month 1", month1Name);
        dataset.addValue(month2Total, "Month 2", month2Name);

        JFreeChart chart = ChartFactory.createBarChart(
                "Month-to-Month Comparison - " + year,
                "Month",
                "Amount (RM)",
                dataset
        );

        styleBarChart(chart, true);
        return chart;
    }

    /**
     * Create yearly bar chart showing yearly trends
     * @param yearlyTotals map of year to total expenses
     * @return styled JFreeChart
     */
    public static JFreeChart createYearlyBarChart(Map<Integer, Double> yearlyTotals) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Double> entry : yearlyTotals.entrySet()) {
            dataset.addValue(entry.getValue(), "Expenses", String.valueOf(entry.getKey()));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Yearly Expenses Comparison",
                "Year",
                "Amount (RM)",
                dataset
        );

        styleBarChart(chart, false);
        return chart;
    }

    /**
     * Apply modern styling to bar chart
     */
    private static void styleBarChart(JFreeChart chart, boolean isComparison) {
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        chart.getLegend().setFrame(BlockBorder.NONE);

        // Background
        plot.setBackgroundPaint(null);
        plot.setOutlineVisible(false);
        
        // Grid lines
        plot.setRangeGridlinePaint(GRID_COLOR);
        plot.setRangeGridlineStroke(new BasicStroke(0.5f));
        
        // Axis styling
        plot.getDomainAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        plot.getDomainAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.getDomainAxis().setLabelPaint(AXIS_COLOR);
        plot.getDomainAxis().setTickLabelPaint(AXIS_COLOR);
        
        plot.getRangeAxis().setLabelFont(new Font("Segoe UI", Font.BOLD, 11));
        plot.getRangeAxis().setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 10));
        plot.getRangeAxis().setLabelPaint(AXIS_COLOR);
        plot.getRangeAxis().setTickLabelPaint(AXIS_COLOR);
        
        // Renderer (bar styling)
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        
        if (isComparison) {
            // Two different colors for comparison
            renderer.setSeriesPaint(0, BAR_COLOR);
            renderer.setSeriesPaint(1, BAR_COLOR_SECONDARY);
        } else {
            // Single color for other charts
            renderer.setSeriesPaint(0, BAR_COLOR);
        }
        
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.75);
        
        // Legend
        chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        // Title
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
        chart.getTitle().setPaint(new Color(17, 24, 39));
    }

    /**
     * Helper method to convert month number to month name
     */
    private static String getMonthName(int month) {
        String[] months = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return (month >= 1 && month <= 12) ? months[month] : "M" + month;
    }
}
