package chart; // Declares that this class belongs to the chart package

// Import JFreeChart classes
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.data.category.DefaultCategoryDataset;

// Import shape class for data point circles
import java.awt.geom.Ellipse2D;

// Import Java utility and GUI classes
import java.awt.*;
import java.util.Map;

/**
 * Class used to create modern line charts
 */
public class ChartLine {

    // ===== COLORS =====

    // Blue line color
    private static final Color LINE_COLOR_PRIMARY =
            new Color(59, 130, 246);

    // Green line color
    private static final Color LINE_COLOR_SECONDARY =
            new Color(16, 185, 129);

    // Axis text color
    private static final Color AXIS_COLOR =
            new Color(75, 85, 99);

    // Grid line color
    private static final Color GRID_COLOR =
            new Color(229, 231, 235);

    // Thickness of line
    private static final Stroke LINE_STROKE =
            new BasicStroke(2.5f);

    /**
     * Create monthly expense trend chart
     */
    public static JFreeChart createMonthlyTrendChart(
            Map<Integer, Double> monthlyTotals,
            int year) {

        // Create dataset
        DefaultCategoryDataset dataset =
                new DefaultCategoryDataset();

        // Loop through monthly data
        for (Map.Entry<Integer, Double> entry
                : monthlyTotals.entrySet()) {

            // Convert month number to month name
            String monthName =
                    getMonthName(entry.getKey());

            // Add data into dataset
            dataset.addValue(
                    entry.getValue(),
                    "Monthly Expenses",
                    monthName
            );
        }

        // Create line chart
        JFreeChart chart =
                ChartFactory.createLineChart(
                        "Monthly Expenses Trend - " + year,
                        "Month",
                        "Amount (RM)",
                        dataset
                );

        // Apply styling
        styleLineChart(chart, false);

        // Enable smooth line settings
        enableSmoothLineRendering(chart);

        // Return chart
        return chart;
    }

    /**
     * Create yearly expense trend chart
     */
    public static JFreeChart createYearlyTrendChart(
            Map<Integer, Double> yearlyTotals) {

        // Create dataset
        DefaultCategoryDataset dataset =
                new DefaultCategoryDataset();

        // Loop through yearly data
        for (Map.Entry<Integer, Double> entry
                : yearlyTotals.entrySet()) {

            // Add yearly data into dataset
            dataset.addValue(
                    entry.getValue(),
                    "Yearly Expenses",
                    String.valueOf(entry.getKey())
            );
        }

        // Create chart
        JFreeChart chart =
                ChartFactory.createLineChart(
                        "Yearly Expenses Trend",
                        "Year",
                        "Amount (RM)",
                        dataset
                );

        // Apply style
        styleLineChart(chart, false);

        // Enable smooth rendering
        enableSmoothLineRendering(chart);

        // Return chart
        return chart;
    }

    /**
     * Create category trend chart
     */
    public static JFreeChart createCategoryTrendChart(
            Map<Integer, Double> monthlyCategoryTotals,
            int year,
            String category) {

        // Create dataset
        DefaultCategoryDataset dataset =
                new DefaultCategoryDataset();

        // Loop through category data
        for (Map.Entry<Integer, Double> entry
                : monthlyCategoryTotals.entrySet()) {

            // Convert month number to month name
            String monthName =
                    getMonthName(entry.getKey());

            // Add category data into dataset
            dataset.addValue(
                    entry.getValue(),
                    category,
                    monthName
            );
        }

        // Create chart
        JFreeChart chart =
                ChartFactory.createLineChart(
                        "Monthly Trend - " + category + " (" + year + ")",
                        "Month",
                        "Amount (RM)",
                        dataset
                );

        // Enable smooth rendering
        enableSmoothLineRendering(chart);

        // Apply styling
        styleLineChart(chart, true);

        // Return chart
        return chart;
    }

    /**
     * Apply styling to line chart
     */
    private static void styleLineChart(
            JFreeChart chart,
            boolean isCategorySpecific) {

        // Get chart plot
        CategoryPlot plot =
                (CategoryPlot) chart.getPlot();

        // Remove legend border
        chart.getLegend().setFrame(BlockBorder.NONE);

        // Remove plot background
        plot.setBackgroundPaint(null);

        // Hide plot border
        plot.setOutlineVisible(false);

        // Set grid line color
        plot.setRangeGridlinePaint(GRID_COLOR);

        // Set grid line thickness
        plot.setRangeGridlineStroke(
                new BasicStroke(0.5f));

        // ===== X-AXIS STYLE =====

        // Set X-axis label font
        plot.getDomainAxis().setLabelFont(
                new Font("Segoe UI", Font.BOLD, 11));

        // Set X-axis text font
        plot.getDomainAxis().setTickLabelFont(
                new Font("Segoe UI", Font.PLAIN, 10));

        // Set X-axis label color
        plot.getDomainAxis().setLabelPaint(AXIS_COLOR);

        // Set X-axis text color
        plot.getDomainAxis().setTickLabelPaint(AXIS_COLOR);

        // ===== Y-AXIS STYLE =====

        // Set Y-axis label font
        plot.getRangeAxis().setLabelFont(
                new Font("Segoe UI", Font.BOLD, 11));

        // Set Y-axis text font
        plot.getRangeAxis().setTickLabelFont(
                new Font("Segoe UI", Font.PLAIN, 10));

        // Set Y-axis label color
        plot.getRangeAxis().setLabelPaint(AXIS_COLOR);

        // Set Y-axis text color
        plot.getRangeAxis().setTickLabelPaint(AXIS_COLOR);

        // Auto-scale Y-axis
        if (plot.getRangeAxis() instanceof NumberAxis) {

            // Convert axis to NumberAxis
            NumberAxis rangeAxis =
                    (NumberAxis) plot.getRangeAxis();

            // Do not force axis to start from 0
            rangeAxis.setAutoRangeIncludesZero(false);
        }

        // ===== LINE STYLE =====

        // Get line renderer
        LineAndShapeRenderer renderer =
                (LineAndShapeRenderer) plot.getRenderer();

        // Set line color
        renderer.setSeriesPaint(
                0,
                isCategorySpecific
                        ? LINE_COLOR_SECONDARY
                        : LINE_COLOR_PRIMARY
        );

        // Set line thickness
        renderer.setSeriesStroke(0, LINE_STROKE);

        // Show points on line
        renderer.setSeriesShapesVisible(0, true);

        // Create circle shape for points
        Shape shape =
                new Ellipse2D.Double(-3, -3, 6, 6);

        // Apply point shape
        renderer.setSeriesShape(0, shape);

        // ===== LEGEND STYLE =====

        // Set legend font
        chart.getLegend().setItemFont(
                new Font("Segoe UI", Font.PLAIN, 11));

        // ===== TITLE STYLE =====

        // Set title font
        chart.getTitle().setFont(
                new Font("Segoe UI", Font.BOLD, 14));

        // Set title color
        chart.getTitle().setPaint(
                new Color(17, 24, 39));
    }

    /**
     * Enable smooth line rendering
     */
    private static void enableSmoothLineRendering(
            JFreeChart chart) {

        // Get chart plot
        CategoryPlot plot =
                (CategoryPlot) chart.getPlot();

        // Get renderer
        LineAndShapeRenderer renderer =
                (LineAndShapeRenderer) plot.getRenderer();

        // Show points on line
        renderer.setSeriesShapesVisible(0, true);

        // Show line
        renderer.setSeriesLinesVisible(0, true);

        // Create larger circle points
        Shape shape =
                new Ellipse2D.Double(-4, -4, 8, 8);

        // Apply shape
        renderer.setSeriesShape(0, shape);
    }

    /**
     * Convert month number into month name
     */
    private static String getMonthName(int month) {

        // Array of month names
        String[] months = {
                "",
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December"
        };

        // Return valid month name
        // Otherwise return fallback text
        return (month >= 1 && month <= 12)
                ? months[month]
                : "Month " + month;
    }
}