package chart; // Declares that this class belongs to the chart package

// Import JFreeChart classes
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleEdge;

// Import Java AWT classes
import java.awt.*;

// Import Map class
import java.util.Map;

/**
 * Class used to create modern pie charts
 */
public class ChartPie {

    // ===== PIE CHART COLORS =====

    // Array of colors for pie slices
    private static final Color[] MODERN_COLORS = {

            new Color(112, 135, 53),
            new Color(182, 193, 84),
            new Color(250, 228, 119),
            new Color(252, 160, 52),
            new Color(208, 103, 28),
            new Color(150, 64, 89),
            new Color(217, 102, 129),
            new Color(240, 123, 220),
            new Color(96, 56, 96)
    };

    /**
     * Create yearly pie chart
     */
    public static JFreeChart createYearlyPieChart(
            Map<String, Double> categoryTotals,
            int year) {

        // Create dataset from category data
        DefaultPieDataset dataset =
                createDataset(categoryTotals);

        // Create pie chart
        JFreeChart chart =
                ChartFactory.createPieChart(
                        "Yearly Expenses by Category - " + year,
                        dataset,
                        true,   // Show legend
                        true,   // Enable tooltip
                        false   // Disable URL
                );

        // Apply styling
        stylePieChart(chart);

        // Configure legend position
        configurePieLegend(chart);

        // Return chart
        return chart;
    }

    /**
     * Create monthly pie chart
     */
    public static JFreeChart createMonthlyPieChart(
            Map<String, Double> categoryTotals,
            int year,
            int month) {

        // Create dataset
        DefaultPieDataset dataset =
                createDataset(categoryTotals);

        // Convert month number to month name
        String monthName =
                getMonthName(month);

        // Create pie chart
        JFreeChart chart =
                ChartFactory.createPieChart(
                        "Monthly Expenses by Category - "
                                + monthName + " " + year,
                        dataset,
                        true,
                        true,
                        false
                );

        // Apply style
        stylePieChart(chart);

        // Configure legend
        configurePieLegend(chart);

        // Return chart
        return chart;
    }

    /**
     * Create dataset from category totals
     */
    private static DefaultPieDataset createDataset(
            Map<String, Double> categoryTotals) {

        // Create dataset object
        DefaultPieDataset dataset =
                new DefaultPieDataset();

        // Loop through category data
        for (Map.Entry<String, Double> entry
                : categoryTotals.entrySet()) {

            // Add category and value into dataset
            dataset.setValue(
                    entry.getKey(),
                    entry.getValue()
            );
        }

        // Return dataset
        return dataset;
    }

    /**
     * Apply style to pie chart
     */
    private static void stylePieChart(
            JFreeChart chart) {

        // Get pie plot from chart
        PiePlot plot =
                (PiePlot) chart.getPlot();

        // Remove legend border
        chart.getLegend().setFrame(BlockBorder.NONE);

        // ===== APPLY COLORS TO PIE SLICES =====

        // Start color index
        int colorIndex = 0;

        // Loop through all pie sections
        for (Object key : plot.getDataset().getKeys()) {

            // Select color from array
            Color color =
                    MODERN_COLORS[
                            colorIndex % MODERN_COLORS.length
                    ];

            // Apply color to section
            plot.setSectionPaint(
                    (Comparable<?>) key,
                    color
            );

            // Move to next color
            colorIndex++;
        }

        // ===== BACKGROUND STYLE =====

        // Remove background color
        plot.setBackgroundPaint(null);

        // Hide plot outline
        plot.setOutlineVisible(false);

        // ===== LABEL STYLE =====

        // Set label font
        plot.setLabelFont(
                new Font("Segoe UI", Font.PLAIN, 10));

        // Set label color
        plot.setLabelPaint(
                new Color(17, 24, 39));

        // Remove label background
        plot.setLabelBackgroundPaint(null);

        // Remove label shadow
        plot.setLabelShadowPaint(null);

        // Remove label outline
        plot.setLabelOutlinePaint(null);

        // Remove label outline stroke
        plot.setLabelOutlineStroke(null);

        // ===== REMOVE PIE SHADOW =====

        // Remove horizontal shadow
        plot.setShadowXOffset(0);

        // Remove vertical shadow
        plot.setShadowYOffset(0);

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
     * Set legend position below chart
     */
    private static void configurePieLegend(
            JFreeChart chart) {

        // Move legend to bottom
        chart.getLegend().setPosition(
                RectangleEdge.BOTTOM);
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