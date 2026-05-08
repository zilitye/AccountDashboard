package com.ex.calculate;

import chart.ChartFrame;
import chart.ChartPie;
import chart.ChartBar;
import chart.ChartLine;
import chart.SQLConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpensesComputeApplication extends JFrame {
    private JComboBox<String> chartTypeBox;
    private JComboBox<Integer> monthBox;
    private JComboBox<Integer> yearBox;
    
    private JLabel yearlyTotalLabel, monthTotalLabel, compareLabel, avgLabel;
    private JPanel chartPanel;
    private ExpensesCompute compute;

    public ExpensesComputeApplication() {
        super("Account Dashboard");
        compute = new ExpensesCompute();

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Integer[] years = {2023, 2024, 2025, 2026, 2027};
        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        
        yearBox = new JComboBox<>(years);
        monthBox = new JComboBox<>(months);
        chartTypeBox = new JComboBox<>(new String[]{
                "Yearly Pie Chart", "Monthly Pie Chart", "Category Breakdown", "Monthly Trend Line"
        });

        LocalDate now = LocalDate.now();
        yearBox.setSelectedItem(now.getYear());
        monthBox.setSelectedItem(now.getMonthValue());

        controlPanel.add(new JLabel("Year:"));
        controlPanel.add(yearBox);
        controlPanel.add(new JLabel("Month:"));
        controlPanel.add(monthBox);
        controlPanel.add(new JLabel("Chart:"));
        controlPanel.add(chartTypeBox);
        topPanel.add(controlPanel);

        // Labels
        yearlyTotalLabel = new JLabel();
        monthTotalLabel = new JLabel();
        compareLabel = new JLabel();
        avgLabel = new JLabel();
        
        topPanel.add(yearlyTotalLabel);
        topPanel.add(monthTotalLabel);
        topPanel.add(compareLabel);
        topPanel.add(avgLabel);

        add(topPanel, BorderLayout.NORTH);

        chartPanel = new JPanel(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);

        yearBox.addActionListener(e -> updateDashboard());
        monthBox.addActionListener(e -> updateDashboard());
        chartTypeBox.addActionListener(e -> updateDashboard());

        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        updateDashboard(); 
    }

    private void updateDashboard() {
        int year = (Integer) yearBox.getSelectedItem();
        int month = (Integer) monthBox.getSelectedItem();
        
        int prevMonth = (month == 1) ? 12 : month - 1;
        int prevYear = (month == 1) ? year - 1 : year;

        // 1. Fetch raw arrays from database
        int[] yearlyData = getExpensesArray(year, null);
        int[] currentMonthData = getExpensesArray(year, month);
        int[] prevMonthData = getExpensesArray(prevYear, prevMonth);

        // 2. Use the ExpensesCompute bean (Strictly following Rubric rules)
        int yearlyTotal = compute.getYearlyTotalExpenses(yearlyData);
        int currentMonthTotal = compute.getMonthlyTotalExpenses(currentMonthData);
        int prevMonthTotal = compute.getMonthlyTotalExpenses(prevMonthData);
        
        double avgMonthly = compute.getAverageMonthlyExpenses(yearlyTotal, 12);
        int monthDiff = compute.getMonthToMonthComparison(prevMonthTotal, currentMonthTotal);
        double pctChange = compute.getPercentageChangeBetweenMonths(prevMonthTotal, currentMonthTotal);

        // 3. Update Labels text
        yearlyTotalLabel.setText(String.format("Yearly Total (%d): $%d", year, yearlyTotal));
        monthTotalLabel.setText(String.format("Current Month Expenses: $%d", currentMonthTotal));
        
        String diffText = (monthDiff >= 0) ? "Increase of $" + monthDiff : "Decrease of $" + Math.abs(monthDiff);
        compareLabel.setText(String.format("Month-to-Month (%d vs %d): %s (%.2f%%)", prevMonth, month, diffText, pctChange));
        avgLabel.setText(String.format("Avg Monthly Expenses: $%.2f", avgMonthly));

        // 4. Update Chart display
        chartPanel.removeAll();
        String selected = (String) chartTypeBox.getSelectedItem();

        if ("Yearly Pie Chart".equals(selected)) {
            chartPanel.add(new ChartFrame(ChartPie.createYearlyPieChart(getCategoryMap(year, null), year)));
        } else if ("Monthly Pie Chart".equals(selected)) {
            chartPanel.add(new ChartFrame(ChartPie.createMonthlyPieChart(getCategoryMap(year, month), year, month)));
        } else if ("Category Breakdown".equals(selected)) {
            chartPanel.add(new ChartFrame(ChartBar.createCategoryBarChart(getCategoryMap(year, month), "Category Breakdown", "Category", "Amount")));
        } else if ("Monthly Trend Line".equals(selected)) {
            chartPanel.add(new ChartFrame(ChartLine.createMonthlyTrendChart(getMonthlyTrendMap(year), year)));
        }

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    // --- Database Helper Methods (Moved here so the Bean stays pure math) ---

    private int[] getExpensesArray(int year, Integer month) {
        List<Integer> list = new ArrayList<>();
        String sql = (month == null) ? 
            "SELECT amount FROM expenses WHERE year = ?" : 
            "SELECT amount FROM expenses WHERE year = ? AND month = ?";
            
        try (Connection conn = SQLConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            if (month != null) ps.setInt(2, month);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add((int) rs.getDouble(1)); // Convert to int array per rubric
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list.stream().mapToInt(i -> i).toArray();
    }

    private Map<String, Double> getCategoryMap(int year, Integer month) {
        Map<String, Double> map = new HashMap<>();
        String sql = (month == null) ? 
            "SELECT category, SUM(amount) FROM expenses WHERE year = ? GROUP BY category" : 
            "SELECT category, SUM(amount) FROM expenses WHERE year = ? AND month = ? GROUP BY category";
            
        try (Connection conn = SQLConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            if (month != null) ps.setInt(2, month);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString(1), rs.getDouble(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private Map<Integer, Double> getMonthlyTrendMap(int year) {
        Map<Integer, Double> map = new HashMap<>();
        String sql = "SELECT month, SUM(amount) FROM expenses WHERE year = ? GROUP BY month";
        try (Connection conn = SQLConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getInt(1), rs.getDouble(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpensesComputeApplication::new);
    }
}