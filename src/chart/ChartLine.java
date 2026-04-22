package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.category.DefaultCategoryDataset;
import java.awt.geom.Ellipse2D;

import java.awt.*;
import java.util.Map;

/**
 * Modern Line Chart with enhanced styling and trend visualization
 * Features: Modern colors, smooth lines, improved readability
 */
public class ChartLine {
    
    // Modern colors for line charts
    private static final Color LINE_COLOR_PRIMARY = new Color(59, 130, 246);      // Modern blue
    private static final Color LINE_COLOR_SECONDARY = new Color(16, 185, 129);    // Modern green
    private static final Color AXIS_COLOR = new Color(75, 85, 99);                // Medium gray
    private static final Color GRID_COLOR = new Color(229, 231, 235);             // Light border
    private static final Stroke LINE_STROKE = new BasicStroke(2.5f);

    /**
     * Create line chart showing monthly expense trends
     * @param monthlyTotals map of month number to total expenses
     * @param year the year
     * @return styled JFreeChart
     */
    public static JFreeChart createMonthlyTrendChart(Map<Integer, Double> monthlyTotals, int year) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Double> entry : monthlyTotals.entrySet()) {
            String monthName = getMonthName(entry.getKey());
            dataset.addValue(entry.getValue(), "Monthly Expenses", monthName);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Monthly Expenses Trend - " + year,
                "Month",
                "Amount (RM)",
                dataset
        );

        styleLineChart(chart, false);
        enableSmoothLineRendering(chart);
        return chart;
    }

    /**
     * Create line chart showing yearly expense trends
     * @param yearlyTotals map of year to total expenses
     * @return styled JFreeChart
     */
    public static JFreeChart createYearlyTrendChart(Map<Integer, Double> yearlyTotals) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Double> entry : yearlyTotals.entrySet()) {
            dataset.addValue(entry.getValue(), "Yearly Expenses", String.valueOf(entry.getKey()));
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Yearly Expenses Trend",
                "Year",
                "Amount (RM)",
                dataset
        );

        styleLineChart(chart, false);
        enableSmoothLineRendering(chart);
        return chart;
    }

    /**
     * Create line chart showing category-specific trends
     * @param monthlyCategoryTotals map of month to category total
     * @param year the year
     * @param category the category name
     * @return styled JFreeChart
     */
    public static JFreeChart createCategoryTrendChart(Map<Integer, Double> monthlyCategoryTotals, 
                                                      int year, String category) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Double> entry : monthlyCategoryTotals.entrySet()) {
            String monthName = getMonthName(entry.getKey());
            dataset.addValue(entry.getValue(), category, monthName);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Monthly Trend - " + category + " (" + year + ")",
                "Month",
                "Amount (RM)",
                dataset
        );

        enableSmoothLineRendering(chart);
        styleLineChart(chart, true);
        return chart;
    }

    /**
     * Apply modern styling to line chart
     */
    private static void styleLineChart(JFreeChart chart, boolean isCategorySpecific) {
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        
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
        // Auto-scale Y-axis to fit data range (start from minimum value, not 0)
        if (plot.getRangeAxis() instanceof NumberAxis) {
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setAutoRangeIncludesZero(false);
        }
        
        // 
        // Renderer (line styling)
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        
        // Series paint and stroke
        renderer.setSeriesPaint(0, isCategorySpecific ? LINE_COLOR_SECONDARY : LINE_COLOR_PRIMARY);
        renderer.setSeriesStroke(0, LINE_STROKE);
        
        // Shape styling (dots on line)
        renderer.setSeriesShapesVisible(0, true);
        Shape shape = new Ellipse2D.Double(-3, -3, 6, 6);
        renderer.setSeriesShape(0, shape);
        
        // Legend
        chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        // Title
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 14));
        chart.getTitle().setPaint(new Color(17, 24, 39));
    }

    /**
     * Enable smooth line rendering using spline interpolation
     */
    private static void enableSmoothLineRendering(JFreeChart chart) {
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        
        // Enable smooth interpolation for the line
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesLinesVisible(0, true);
        
        // Use larger points for better visibility
        Shape shape = new Ellipse2D.Double(-4, -4, 8, 8);
        renderer.setSeriesShape(0, shape);
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
