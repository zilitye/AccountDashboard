package com.ex.calculate;

import chart.ChartFrame;
import chart.ChartPie;
import chart.ChartBar;
import chart.ChartLine;

import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;

public class ExpensesComputeApplication extends JFrame {
    private JComboBox<String> categoryBox;
    private JTextField amountField;
    private JButton addButton;
    private JComboBox<String> chartTypeBox;
    private JPanel chartPanel;
    private JComboBox<String> monthSelector;
    private JLabel currentMonthSpendingLabel;
    private JLabel currentMonthNameLabel;

    private ExpensesCompute compute;
    private int selectedYear = LocalDate.now().getYear();
    private int selectedMonth = LocalDate.now().getMonthValue();

    public ExpensesComputeApplication() {
        super("Account Dashboard");
        compute = new ExpensesCompute();

        // Modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore and use default
        }

        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Color.WHITE);

        // ===== TOP BAR =====
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(25, 25, 25));
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("💰 Account Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        topBar.add(titleLabel, BorderLayout.WEST);

        // Month selector
        JPanel monthPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        monthPanel.setBackground(new Color(25, 25, 25));

        JLabel monthLabel = new JLabel("Month:");
        monthLabel.setForeground(Color.WHITE);
        monthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        monthPanel.add(monthLabel);

        String[] months = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        monthSelector = new JComboBox<>(months);
        monthSelector.setSelectedIndex(selectedMonth - 1);
        monthSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        monthSelector.setBackground(Color.WHITE);
        monthSelector.setPreferredSize(new Dimension(120, 30));
        monthSelector.addActionListener(e -> updateSelectedMonth());
        monthPanel.add(monthSelector);

        topBar.add(monthPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ===== MAIN CONTENT =====
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);

        // ===== LEFT SIDEBAR =====
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sidebar.setPreferredSize(new Dimension(280, 0));

        // Current spending card
        JPanel spendingCard = createSpendingCard();
        sidebar.add(spendingCard, BorderLayout.NORTH);

        // Input form
        JPanel inputPanel = createInputPanel();
        sidebar.add(inputPanel, BorderLayout.CENTER);

        mainPanel.add(sidebar, BorderLayout.WEST);

        // ===== RIGHT CONTENT =====
        JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(Color.WHITE);

        // Chart selector
        JPanel chartSelectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        chartSelectorPanel.setBackground(Color.WHITE);

        JLabel chartLabel = new JLabel("Chart Type");
        chartLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chartLabel.setForeground(new Color(50, 50, 50));
        chartSelectorPanel.add(chartLabel);

        chartTypeBox = new JComboBox<>(new String[]{
            "Monthly Breakdown",
            "Yearly Overview",
            "Monthly Pie",
            "Month Comparison",
            "Trend Analysis",
            "Average Expenses"
        });
        chartTypeBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        chartTypeBox.setBackground(Color.WHITE);
        chartTypeBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        chartTypeBox.setPreferredSize(new Dimension(200, 35));
        chartTypeBox.addActionListener(e -> updateChart());
        chartSelectorPanel.add(chartTypeBox);

        contentPanel.add(chartSelectorPanel, BorderLayout.NORTH);

        // Chart area
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(240, 240, 240), 1));
        contentPanel.add(chartPanel, BorderLayout.CENTER);

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        setSize(1300, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        SwingUtilities.invokeLater(this::updateChart);
    }

    private JPanel createSpendingCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(240, 240, 240), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        // Title
        currentMonthNameLabel = new JLabel("Current Month");
        currentMonthNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        currentMonthNameLabel.setForeground(new Color(100, 100, 100));
        card.add(currentMonthNameLabel, BorderLayout.NORTH);

        // Amount
        currentMonthSpendingLabel = new JLabel("$ 0.00");
        currentMonthSpendingLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        currentMonthSpendingLabel.setForeground(new Color(25, 25, 25));
        currentMonthSpendingLabel.setHorizontalAlignment(JLabel.CENTER);
        card.add(currentMonthSpendingLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        // Category
        JPanel categoryPanel = new JPanel(new BorderLayout());
        categoryPanel.setBackground(Color.WHITE);
        categoryPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        categoryBox = new JComboBox<>(new String[]{
            "Food & Beverages",
            "Entertainment",
            "Leisure & Sports",
            "Services",
            "Shopping",
            "Telecom",
            "Utilities"
        });
        categoryBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        categoryPanel.add(categoryBox, BorderLayout.CENTER);
        panel.add(categoryPanel);

        // Amount
        JPanel amountPanel = new JPanel(new BorderLayout());
        amountPanel.setBackground(Color.WHITE);
        amountPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        amountField = new JTextField();
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        amountField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        amountPanel.add(amountField, BorderLayout.CENTER);
        panel.add(amountPanel);

        // Add button
        addButton = new JButton("Add Expense");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addButton.setBackground(new Color(25, 25, 25));
        addButton.setForeground(Color.BLACK);
        addButton.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setFocusPainted(false);
        addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        addButton.addActionListener(this::handleAddExpense);
        panel.add(addButton);

        return panel;
    }

    private void updateSelectedMonth() {
        selectedMonth = monthSelector.getSelectedIndex() + 1;
        updateChart();
    }

    private void handleAddExpense(ActionEvent e) {
        try {
            String category = (String) categoryBox.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText());

            compute.addExpense(selectedYear, selectedMonth, category, amount);
            amountField.setText("");
            updateChart();

            JOptionPane.showMessageDialog(this, "Expense added successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.",
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateChart() {
        chartPanel.removeAll();
        String selected = (String) chartTypeBox.getSelectedItem();

        try {
            // Update current month spending
            double monthlyTotal = compute.getMonthlyTotal(selectedYear, selectedMonth);
            currentMonthSpendingLabel.setText(String.format("$ %.2f", monthlyTotal));
            currentMonthNameLabel.setText(monthSelector.getItemAt(selectedMonth - 1));

            JPanel chartDisplay = null;

            switch (selected) {
                case "Monthly Breakdown":
                    JFreeChart barChart = ChartBar.createCategoryBarChart(
                        compute.getTotalsByCategory(selectedYear, selectedMonth),
                        "Monthly Breakdown",
                        "Category", "Amount"
                    );
                    chartDisplay = new ChartFrame(barChart);
                    break;

                case "Yearly Overview":
                    JFreeChart yearlyPie = ChartPie.createYearlyPieChart(
                        compute.getTotalsByCategory(selectedYear, null),
                        selectedYear
                    );
                    chartDisplay = new ChartFrame(yearlyPie);
                    break;

                case "Monthly Pie":
                    JFreeChart monthlyPie = ChartPie.createMonthlyPieChart(
                        compute.getTotalsByCategory(selectedYear, selectedMonth),
                        selectedYear, selectedMonth
                    );
                    chartDisplay = new ChartFrame(monthlyPie);
                    break;

                case "Month Comparison":
                    int prevMonth = (selectedMonth == 1) ? 12 : selectedMonth - 1;
                    int comparisonYear = (selectedMonth == 1) ? selectedYear - 1 : selectedYear;
                    JFreeChart comparisonChart = ChartBar.createMonthComparisonChart(
                        compute.getMonthlyTotal(comparisonYear, prevMonth),
                        compute.getMonthlyTotal(selectedYear, selectedMonth),
                        prevMonth, selectedMonth, selectedYear
                    );
                    chartDisplay = new ChartFrame(comparisonChart);
                    break;

                case "Trend Analysis":
                    JFreeChart trendChart = ChartLine.createMonthlyTrendChart(
                        compute.getMonthlyTotals(selectedYear),
                        selectedYear
                    );
                    chartDisplay = new ChartFrame(trendChart);
                    break;

                case "Average Expenses":
                    JPanel avgPanel = new JPanel(new BorderLayout());
                    avgPanel.setBackground(Color.WHITE);
                    double avg = compute.getAverageMonthlyExpenses(selectedYear);
                    JLabel avgLabel = new JLabel(String.format("$ %.2f", avg));
                    avgLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
                    avgLabel.setHorizontalAlignment(JLabel.CENTER);
                    avgLabel.setForeground(new Color(25, 25, 25));
                    avgPanel.add(avgLabel, BorderLayout.CENTER);

                    JLabel avgTitle = new JLabel("Average Monthly Expenses");
                    avgTitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                    avgTitle.setHorizontalAlignment(JLabel.CENTER);
                    avgTitle.setForeground(new Color(100, 100, 100));
                    avgPanel.add(avgTitle, BorderLayout.NORTH);

                    chartDisplay = avgPanel;
                    break;
            }

            if (chartDisplay != null) {
                chartPanel.add(chartDisplay, BorderLayout.CENTER);
            }
        } catch (Exception ex) {
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.setBackground(Color.WHITE);

            JLabel errorLabel = new JLabel("Database connection required. Please run accountdb.sql first.");
            errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            errorLabel.setHorizontalAlignment(JLabel.CENTER);
            errorLabel.setForeground(new Color(150, 150, 150));
            errorPanel.add(errorLabel, BorderLayout.CENTER);

            chartPanel.add(errorPanel, BorderLayout.CENTER);
        }

        chartPanel.revalidate();
        chartPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpensesComputeApplication::new);
    }
}
