package chart; // Declares that this class belongs to the 'chart' package

// Import JFreeChart classes for creating and styling charts
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

// Import Java utility and GUI classes
import java.awt.*;
import java.util.Map;

/**
 * Modern Bar Chart with enhanced styling and visual hierarchy
 * Features: Modern colors, improved axis labels, better legend formatting
 */
public class ChartBar { // Main class for creating styled bar charts
    
    // Define modern colors used in the chart
    private static final Color BAR_COLOR = new Color(59, 130, 246);      
    // Blue color for main bars

    private static final Color BAR_COLOR_SECONDARY = new Color(16, 185, 129); 
    // Green color for secondary comparison bars

    private static final Color AXIS_COLOR = new Color(75, 85, 99);       
    // Gray color for axis text and labels

    private static final Color GRID_COLOR = new Color(229, 231, 235);    
    // Light gray color for grid lines

    /**
     * Create category bar chart showing expense breakdown
     * @param categoryTotals map of category to amount
     * @param title chart title
     * @param xLabel x-axis label
     * @param yLabel y-axis label
     * @return styled JFreeChart
     */
    public static JFreeChart createCategoryBarChart(
            Map<String, Double> categoryTotals, 
            String title, 
            String xLabel, 
            String yLabel) {

        // Create dataset object to store chart data
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Loop through all category and amount entries
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {

            // Add value into dataset
            // Format: value, row key(series), column key(category)
            dataset.addValue(entry.getValue(), "Expenses", entry.getKey());
        }

        // Create a vertical bar chart
        JFreeChart chart = ChartFactory.createBarChart(
                title,     // Chart title
                xLabel,    // X-axis label
                yLabel,    // Y-axis label
                dataset    // Dataset containing chart data
        );

        // Apply custom styling to the chart
        styleBarChart(chart, false);

        // Return the finished chart
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
    public static JFreeChart createMonthComparisonChart(
            double month1Total, 
            double month2Total, 
            int month1, 
            int month2, 
            int year) {

        // Create dataset for comparison chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Convert month number to short month name
        String month1Name = getMonthName(month1);
        String month2Name = getMonthName(month2);
        
        // Add first month data
        dataset.addValue(month1Total, "Month 1", month1Name);

        // Add second month data
        dataset.addValue(month2Total, "Month 2", month2Name);

        // Create comparison bar chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Month-to-Month Comparison - " + year, // Chart title
                "Month",                               // X-axis label
                "Amount (RM)",                        // Y-axis label
                dataset                               // Data
        );

        // Apply chart styling with comparison colors enabled
        styleBarChart(chart, true);

        // Return the chart
        return chart;
    }

    /**
     * Create yearly bar chart showing yearly trends
     * @param yearlyTotals map of year to total expenses
     * @return styled JFreeChart
     */
    public static JFreeChart createYearlyBarChart(Map<Integer, Double> yearlyTotals) {

        // Create dataset object
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Loop through yearly data
        for (Map.Entry<Integer, Double> entry : yearlyTotals.entrySet()) {

            // Add yearly expense data into dataset
            dataset.addValue(
                    entry.getValue(),               // Expense amount
                    "Expenses",                     // Series name
                    String.valueOf(entry.getKey()) // Year converted to String
            );
        }

        // Create yearly comparison chart
        JFreeChart chart = ChartFactory.createBarChart(
                "Yearly Expenses Comparison", // Title
                "Year",                       // X-axis label
                "Amount (RM)",                // Y-axis label
                dataset                       // Dataset
        );

        // Apply styling
        styleBarChart(chart, false);

        // Return chart
        return chart;
    }

    /**
     * Apply modern styling to bar chart
     */
    private static void styleBarChart(JFreeChart chart, boolean isComparison) {

        // Get plot area of chart
        CategoryPlot plot = (CategoryPlot) chart.getPlot();

        // Remove legend border
        chart.getLegend().setFrame(BlockBorder.NONE);

        // Remove background color
        plot.setBackgroundPaint(null);

        // Hide outline around plot
        plot.setOutlineVisible(false);
        
        // Set grid line color
        plot.setRangeGridlinePaint(GRID_COLOR);

        // Set thickness of grid lines
        plot.setRangeGridlineStroke(new BasicStroke(0.5f));
        
        // ===== DOMAIN AXIS (X-AXIS) STYLING =====

        // Set font for X-axis label
        plot.getDomainAxis().setLabelFont(
                new Font("Segoe UI", Font.BOLD, 11));

        // Set font for X-axis category names
        plot.getDomainAxis().setTickLabelFont(
                new Font("Segoe UI", Font.PLAIN, 10));

        // Set color for X-axis label
        plot.getDomainAxis().setLabelPaint(AXIS_COLOR);

        // Set color for X-axis category text
        plot.getDomainAxis().setTickLabelPaint(AXIS_COLOR);
        
        // ===== RANGE AXIS (Y-AXIS) STYLING =====

        // Set font for Y-axis label
        plot.getRangeAxis().setLabelFont(
                new Font("Segoe UI", Font.BOLD, 11));

        // Set font for Y-axis values
        plot.getRangeAxis().setTickLabelFont(
                new Font("Segoe UI", Font.PLAIN, 10));

        // Set color for Y-axis label
        plot.getRangeAxis().setLabelPaint(AXIS_COLOR);

        // Set color for Y-axis values
        plot.getRangeAxis().setTickLabelPaint(AXIS_COLOR);
        
        // Get chart renderer used for drawing bars
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        
        // Check if chart is comparison chart
        if (isComparison) {

            // Set blue color for first series
            renderer.setSeriesPaint(0, BAR_COLOR);

            // Set green color for second series
            renderer.setSeriesPaint(1, BAR_COLOR_SECONDARY);

        } else {

            // Use single blue color for all bars
            renderer.setSeriesPaint(0, BAR_COLOR);
        }
        
        // Use flat bar design instead of gradient
        renderer.setBarPainter(
                new org.jfree.chart.renderer.category.StandardBarPainter());

        // Remove bar shadow effect
        renderer.setShadowVisible(false);

        // Set maximum width of bars
        renderer.setMaximumBarWidth(0.75);
        
        // ===== LEGEND STYLING =====

        // Set legend font
        chart.getLegend().setItemFont(
                new Font("Segoe UI", Font.PLAIN, 11));
        
        // ===== TITLE STYLING =====

        // Set chart title font
        chart.getTitle().setFont(
                new Font("Segoe UI", Font.BOLD, 14));

        // Set chart title color
        chart.getTitle().setPaint(new Color(17, 24, 39));
    }

    /**
     * Helper method to convert month number to month name
     */
    private static String getMonthName(int month) {

        // Array containing short month names
        String[] months = {
                "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };

        // Return valid month name or fallback value
        return (month >= 1 && month <= 12) 
                ? months[month] 
                : "M" + month;
    }
}