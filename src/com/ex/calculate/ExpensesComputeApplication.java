package com.ex.calculate;

import chart.ChartFrame;
import chart.ChartPie;
import chart.ChartBar;
import chart.ChartLine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;

public class ExpensesComputeApplication extends JFrame {
    private JTextField categoryField;
    private JTextField amountField;
    private JButton addButton;
    private JComboBox<String> chartTypeBox;
    private JPanel chartPanel;

    private ExpensesCompute compute;

    public ExpensesComputeApplication() {
        super("Account Dashboard");
        compute = new ExpensesCompute();

        setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.add(new JLabel("Category:"));
        categoryField = new JTextField();
        inputPanel.add(categoryField);

        inputPanel.add(new JLabel("Amount:"));
        amountField = new JTextField();
        inputPanel.add(amountField);

        addButton = new JButton("Add Expense");
        inputPanel.add(addButton);

        chartTypeBox = new JComboBox<>(new String[]{
            "Yearly Pie Chart",
            "Monthly Pie Chart",
            "Category Breakdown",
            "Average Monthly Expenses",
            "Month-to-Month Comparison",
            "Monthly Trend Line"
        });
        inputPanel.add(chartTypeBox);

        add(inputPanel, BorderLayout.NORTH);

        // Chart panel
        chartPanel = new JPanel(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);

        // Event handling
        addButton.addActionListener(this::handleAddExpense);
        chartTypeBox.addActionListener(e -> updateChart());

        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void handleAddExpense(ActionEvent e) {
        try {
            String category = categoryField.getText();
            double amount = Double.parseDouble(amountField.getText());

            LocalDate now = LocalDate.now();
            int year = now.getYear();
            int month = now.getMonthValue();

            compute.addExpense(year, month, category, amount);
            updateChart();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.");
        }
    }

    private void updateChart() {
        chartPanel.removeAll();
        String selected = (String) chartTypeBox.getSelectedItem();

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        switch (selected) {
            case "Yearly Pie Chart":
                chartPanel.add(new ChartFrame(
                    ChartPie.createYearlyPieChart(compute.getTotalsByCategory(year, null), year)
                ), BorderLayout.CENTER);
                break;

            case "Monthly Pie Chart":
                chartPanel.add(new ChartFrame(
                    ChartPie.createMonthlyPieChart(compute.getTotalsByCategory(year, month), year, month)
                ), BorderLayout.CENTER);
                break;

            case "Category Breakdown":
                chartPanel.add(new ChartFrame(
                    ChartBar.createCategoryBarChart(compute.getTotalsByCategory(year, month),
                        "Category Breakdown (" + month + "/" + year + ")", "Category", "Amount")
                ), BorderLayout.CENTER);
                break;

            case "Average Monthly Expenses":
                double avg = compute.getAverageMonthlyExpenses(year);
                chartPanel.add(new JLabel("Average Monthly Expenses: " + avg), BorderLayout.CENTER);
                break;

            case "Month-to-Month Comparison":
                int prevMonth = (month == 1) ? 12 : month - 1;
                chartPanel.add(new ChartFrame(
                    ChartBar.createMonthComparisonChart(
                        compute.getMonthlyTotal(year, prevMonth),
                        compute.getMonthlyTotal(year, month),
                        prevMonth, month, year)
                ), BorderLayout.CENTER);
                break;

            case "Monthly Trend Line":
                chartPanel.add(new ChartFrame(
                    ChartLine.createMonthlyTrendChart(compute.getMonthlyTotals(year), year)
                ), BorderLayout.CENTER);
                break;
        }

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpensesComputeApplication::new);
    }
}
