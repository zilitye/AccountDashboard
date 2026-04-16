package chart;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;

public class ChartFrame extends JFrame {

    public ChartFrame(String chartType) {
        setTitle("Account Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel chartPanel;

        // Decide which chart to show
        if ("bar".equalsIgnoreCase(chartType)) {
            ChartBar chartBar = new ChartBar();
            chartPanel = chartBar.createBarChart();
        } else {
            ChartLine chartLine = new ChartLine();
            chartPanel = chartLine.createLineChart();
        }

        add(chartPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Change "bar" to "line" to switch chart type
            ChartFrame frame = new ChartFrame("bar");
            frame.setVisible(true);
        });
    }
}
