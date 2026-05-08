package chart; // Declares that this class belongs to the 'chart' package

// Import JFreeChart classes
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

// Import Swing classes for GUI
import javax.swing.*;
import javax.swing.border.EmptyBorder;

// Import Java AWT classes for colors, fonts, layouts, etc.
import java.awt.*;

/**
 * Modern ChartFrame with enhanced UI styling
 * Features: Modern color scheme, typography, spacing, and interactivity
 */
public class ChartFrame extends JPanel { 
    // ChartFrame class extends JPanel so it can be used as a GUI panel

    // Declare ChartPanel object to display chart
    private ChartPanel chartPanel;
    
    // ===== MODERN COLOR PALETTE =====

    // Light gray background color for the main frame
    private static final Color COLOR_BACKGROUND = new Color(248, 249, 250);

    // Pure white background for chart area
    private static final Color COLOR_PANEL_BG = new Color(255, 255, 255);

    // Dark charcoal color for main text
    private static final Color COLOR_TEXT_PRIMARY = new Color(17, 24, 39);

    // Medium gray color for secondary text
    private static final Color COLOR_TEXT_SECONDARY = new Color(75, 85, 99);

    // Light gray border color
    private static final Color COLOR_BORDER = new Color(229, 231, 235);

    // Accent color (currently unused)
    //private static final Color COLOR_ACCENT = new Color(59, 130, 246);

    // ===== FONT SETTINGS =====

    // Font used for chart titles
    private static final Font FONT_TITLE = 
            new Font("Segoe UI", Font.BOLD, 16);

    // Font used for labels and axis text
    private static final Font FONT_LABEL = 
            new Font("Segoe UI", Font.PLAIN, 11);

    /**
     * Constructor for ChartFrame
     * Accepts a JFreeChart object
     */
    public ChartFrame(JFreeChart chart) {

        // Initialize the frame with the provided chart
        initializeFrame(chart);
    }

    /**
     * Initialize the panel layout and chart settings
     */
    private void initializeFrame(JFreeChart chart) {

        // Set panel layout to BorderLayout
        setLayout(new BorderLayout());

        // Set background color of panel
        setBackground(COLOR_BACKGROUND);

        // Set empty border around panel
        setBorder(new EmptyBorder(0, 0, 0, 0));

        // Apply custom modern styling to chart
        styleChart(chart);
        
        // Create ChartPanel to display the chart
        chartPanel = new ChartPanel(chart);

        // Set preferred size of chart panel
        chartPanel.setPreferredSize(new Dimension(800, 550));

        // Enable zooming using mouse wheel
        chartPanel.setMouseWheelEnabled(true);

        // Enable mouse drag zooming
        chartPanel.setMouseZoomable(true);

        // Enable vertical zooming
        chartPanel.setRangeZoomable(true);

        // Enable horizontal zooming
        chartPanel.setDomainZoomable(true);

        // Set chart panel background color
        chartPanel.setBackground(COLOR_PANEL_BG);
        
        // Add padding around chart panel
        chartPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        
        // Add chart panel to center of main panel
        add(chartPanel, BorderLayout.CENTER);
    }

    /**
     * Apply modern styling to the chart
     */
    private void styleChart(JFreeChart chart) {

        // Set overall chart background color
        chart.setBackgroundPaint(COLOR_PANEL_BG);
        
        // ===== TITLE STYLING =====

        // Check if chart has a title
        if (chart.getTitle() != null) {

            // Set title font
            chart.getTitle().setFont(FONT_TITLE);

            // Set title text color
            chart.getTitle().setPaint(COLOR_TEXT_PRIMARY);

            // Add margin below title
            chart.getTitle().setMargin(0, 0, 8, 0);
        }
        
        // ===== PLOT STYLING =====

        // Check if chart contains a plot
        if (chart.getPlot() != null) {

            // Hide plot outline border
            chart.getPlot().setOutlineVisible(false);
            
            // ===== CATEGORY PLOT STYLING =====

            // Check if plot is CategoryPlot (bar chart)
            if (chart.getPlot() instanceof org.jfree.chart.plot.CategoryPlot) {

                // Cast plot into CategoryPlot object
                org.jfree.chart.plot.CategoryPlot plot =
                        (org.jfree.chart.plot.CategoryPlot) chart.getPlot();

                // Set grid line color
                plot.setRangeGridlinePaint(COLOR_BORDER);

                // Set thickness of grid lines
                plot.setRangeGridlineStroke(new BasicStroke(0.5f));

                // Set X-axis label font
                plot.getDomainAxis().setLabelFont(FONT_LABEL);

                // Set X-axis category label font
                plot.getDomainAxis().setTickLabelFont(FONT_LABEL);

                // Set Y-axis label font
                plot.getRangeAxis().setLabelFont(FONT_LABEL);

                // Set Y-axis value label font
                plot.getRangeAxis().setTickLabelFont(FONT_LABEL);

                // Set X-axis label color
                plot.getDomainAxis().setLabelPaint(COLOR_TEXT_SECONDARY);

                // Set Y-axis label color
                plot.getRangeAxis().setLabelPaint(COLOR_TEXT_SECONDARY);

            } 
            
            // ===== PIE CHART STYLING =====

            else if (chart.getPlot() instanceof org.jfree.chart.plot.PiePlot) {

                // Cast plot into PiePlot object
                org.jfree.chart.plot.PiePlot plot =
                        (org.jfree.chart.plot.PiePlot) chart.getPlot();

                // Set pie chart label font
                plot.setLabelFont(FONT_LABEL);
            }
        }
    }

    /**
     * Update chart dynamically with modern styling
     */
    public void setChart(JFreeChart chart) {

        // Check if old chart panel exists
        if (chartPanel != null) {

            // Remove old chart panel
            remove(chartPanel);
        }
        
        // Apply modern styling to new chart
        styleChart(chart);
        
        // Create new chart panel
        chartPanel = new ChartPanel(chart);

        // Set preferred chart size
        chartPanel.setPreferredSize(new Dimension(800, 550));

        // Enable mouse wheel zoom
        chartPanel.setMouseWheelEnabled(true);

        // Enable mouse drag zoom
        chartPanel.setMouseZoomable(true);

        // Enable vertical zoom
        chartPanel.setRangeZoomable(true);

        // Enable horizontal zoom
        chartPanel.setDomainZoomable(true);

        // Set chart background color
        chartPanel.setBackground(COLOR_PANEL_BG);
        
        // Create compound border:
        // outer border + inner padding
        chartPanel.setBorder(BorderFactory.createCompoundBorder(

            // Outer line border
            BorderFactory.createLineBorder(COLOR_BORDER, 1),

            // Inner empty padding
            new EmptyBorder(8, 8, 8, 8)
        ));
        
        // Add updated chart panel to center
        add(chartPanel, BorderLayout.CENTER);

        // Refresh layout after update
        revalidate();

        // Repaint panel to show new chart
        repaint();
    }

    /**
     * Get the chart panel for advanced customization
     */
    public ChartPanel getChartPanel() {

        // Return chart panel object
        return chartPanel;
    }
}