package chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JPanel;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ChartLine {

    public JPanel createLineChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try {
            Connection conn = SQLConnection.getInstance().getConnection();
            Statement stmt = conn.createStatement();

            // Example query: adjust table/column names to match your DB schema
            String query = "SELECT month, category, amount FROM expenses WHERE year = 2026";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String month = rs.getString("month");
                String category = rs.getString("category");
                double amount = rs.getDouble("amount");

                dataset.addValue(amount, category, month);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Monthly Expenses Trend",
                "Month",
                "Amount",
                dataset
        );

        return new ChartPanel(lineChart);
    }
}
