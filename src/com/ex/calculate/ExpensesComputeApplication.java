package com.ex.calculate;

import chart.ChartFrame;
import chart.ChartPie;
import chart.ChartBar;
import chart.ChartLine;

import org.jfree.chart.JFreeChart;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.Map;

public class ExpensesComputeApplication extends JFrame {
    private JComboBox<String> categoryBox;
    private JTextField amountField;
    private JButton addButton;
    private JPanel chartLeftPanel;
    private JPanel chartRightPanel;
    private JComboBox<String> monthSelector;
    private JLabel currentMonthSpendingLabel;
    private JLabel currentMonthNameLabel;
    private JLabel averageExpensesLabel;
    private JButton tabButtonMonth, tabButtonYear;
    private int currentViewMode = 1; // 1=Month, 2=Year

    private ExpensesCompute compute;
    private int selectedYear = LocalDate.now().getYear();
    private int selectedMonth = LocalDate.now().getMonthValue();

    // Light theme color palette
    private static final Color COLOR_BACKGROUND  = new Color(245, 246, 250);
    private static final Color COLOR_SURFACE     = new Color(255, 255, 255);
    private static final Color COLOR_CARD        = new Color(255, 255, 255);
    private static final Color COLOR_PRIMARY     = new Color(59, 130, 246);   // blue
    private static final Color COLOR_ACCENT      = new Color(16, 185, 129);   // green
    private static final Color COLOR_DANGER      = new Color(239, 68, 68);    // red
    private static final Color COLOR_TEXT        = new Color(17, 24, 39);
    private static final Color COLOR_TEXT_SEC    = new Color(107, 114, 128);
    private static final Color COLOR_BORDER      = new Color(209, 213, 219);
    private static final int   RADIUS            = 12;

    public ExpensesComputeApplication() {
        super("Expense Dashboard");
        compute = new ExpensesCompute();

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Component.focusWidth", 0);
            UIManager.put("Button.arc", RADIUS);
            UIManager.put("TextComponent.arc", RADIUS);
            UIManager.put("ComboBox.arc", RADIUS);
            UIManager.put("Component.arc", RADIUS);
            UIManager.put("Panel.background", COLOR_BACKGROUND);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(COLOR_BACKGROUND);

        // TOP: tab bar
        add(createTabBar(), BorderLayout.NORTH);

        // CENTER: charts + right panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(COLOR_BACKGROUND);
        
        // Wrap charts container to prevent expansion
        JPanel chartsWrapper = new JPanel();
        chartsWrapper.setLayout(new BoxLayout(chartsWrapper, BoxLayout.X_AXIS));
        chartsWrapper.setBackground(COLOR_BACKGROUND);
        chartsWrapper.add(createChartsContainer());
        chartsWrapper.add(Box.createHorizontalGlue());
        
        mainPanel.add(chartsWrapper, BorderLayout.CENTER);
        mainPanel.add(createRightPanel(), BorderLayout.EAST);
        add(mainPanel, BorderLayout.CENTER);

        setSize(950, 820);
        setMinimumSize(new Dimension(950, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        //setResizable(false);
        setVisible(true);

        SwingUtilities.invokeLater(this::updateCharts);
    }

    // ─────────────────────────────────────────────
    // TAB BAR  (Month | Year  +  month picker)
    // ─────────────────────────────────────────────
    private JPanel createTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        bar.setBackground(COLOR_SURFACE);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));
        bar.setPreferredSize(new Dimension(0, 50));

        tabButtonMonth = createTabButton("Month");
        tabButtonYear  = createTabButton("Year");
        setActiveTab(tabButtonMonth);

        bar.add(tabButtonMonth);
        bar.add(tabButtonYear);
        bar.add(Box.createHorizontalStrut(16));

        JLabel ml = new JLabel("Month:");
        ml.setForeground(COLOR_TEXT_SEC);
        ml.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bar.add(ml);

        String[] months = {"January","February","March","April","May","June",
                           "July","August","September","October","November","December"};
        monthSelector = new JComboBox<>(months);
        monthSelector.setSelectedIndex(selectedMonth - 1);
        monthSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        monthSelector.setPreferredSize(new Dimension(120, 28));
        monthSelector.addActionListener(e -> {
            selectedMonth = monthSelector.getSelectedIndex() + 1;
            updateCharts();
        });
        bar.add(monthSelector);

        // Year spinner
        bar.add(Box.createHorizontalStrut(12));
        JLabel yl = new JLabel("Year:");
        yl.setForeground(COLOR_TEXT_SEC);
        yl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bar.add(yl);

        SpinnerNumberModel yearModel = new SpinnerNumberModel(selectedYear, 2000, 2099, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);
        yearSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        yearSpinner.setPreferredSize(new Dimension(75, 28));
        yearSpinner.addChangeListener(e -> {
            selectedYear = (int) yearSpinner.getValue();
            updateCharts();
        });
        bar.add(yearSpinner);

        return bar;
    }

    private JButton createTabButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        btn.setBackground(COLOR_SURFACE);
        btn.setForeground(COLOR_TEXT_SEC);
        btn.addActionListener(e -> {
            setActiveTab(btn);
            currentViewMode = text.equals("Month") ? 1 : 2;
            updateCharts();
        });
        return btn;
    }

    private void setActiveTab(JButton active) {
        for (JButton b : new JButton[]{tabButtonMonth, tabButtonYear}) {
            if (b == null) continue;
            b.setBackground(COLOR_SURFACE);
            b.setForeground(COLOR_TEXT_SEC);
        }
        active.setBackground(COLOR_PRIMARY);
        active.setForeground(Color.WHITE);
    }

 // ─────────────────────────────────────────────
// CHARTS CONTAINER  (responsive: vertical <-> horizontal)
// ─────────────────────────────────────────────
private JPanel createChartsContainer() {
    JPanel container = new JPanel();
    container.setBackground(COLOR_BACKGROUND);
    container.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 8));

    chartLeftPanel = createCard();
    chartRightPanel = createCard();

    // Default layout: vertical (stacked)
    container.setLayout(new GridLayout(2, 1, 0, 14));
    container.add(chartLeftPanel);
    container.add(chartRightPanel);

    // Add resize listener to parent frame
    this.addComponentListener(new java.awt.event.ComponentAdapter() {
        @Override
        public void componentResized(java.awt.event.ComponentEvent e) {
            int width = getWidth();
            if (width > 1200) { 
                // Switch to horizontal layout when expanded
                container.setLayout(new GridLayout(1, 2, 14, 0));
            } else {
                // Default stacked layout
                container.setLayout(new GridLayout(2, 1, 0, 14));
            }
            container.revalidate();
        }
    });

    return container;
}


    // ─────────────────────────────────────────────
    // RIGHT PANEL  (stats + add expense)
    // ─────────────────────────────────────────────
    private JPanel createRightPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(COLOR_BACKGROUND);
        outer.setPreferredSize(new Dimension(290, 0));
        outer.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 16));

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(COLOR_BACKGROUND);

        // ── Average Expenses card ──
        inner.add(buildStatCard("Average Monthly Expenses",
                                averageExpensesLabelRef(), COLOR_ACCENT));
        inner.add(Box.createVerticalStrut(12));

        // ── Current month spending card ──
        inner.add(buildStatCard("Current Month Spending",
                                currentMonthSpendingLabelRef(), COLOR_PRIMARY));
        inner.add(Box.createVerticalStrut(12));

        // ── Add Expense form card ──
        inner.add(createAddExpenseCard());
        inner.add(Box.createVerticalGlue());

        JScrollPane sp = new JScrollPane(inner);
        sp.setBorder(null);
        sp.setBackground(COLOR_BACKGROUND);
        sp.getViewport().setBackground(COLOR_BACKGROUND);
        outer.add(sp, BorderLayout.CENTER);
        return outer;
    }

    // Helpers to initialise labels before building cards
    private JLabel averageExpensesLabelRef() {
        averageExpensesLabel = new JLabel("RM 0.00");
        averageExpensesLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        averageExpensesLabel.setForeground(COLOR_ACCENT);
        return averageExpensesLabel;
    }

    private JLabel currentMonthSpendingLabelRef() {
        currentMonthSpendingLabel = new JLabel("RM 0.00");
        currentMonthSpendingLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        currentMonthSpendingLabel.setForeground(COLOR_PRIMARY);
        currentMonthNameLabel = new JLabel("–");
        return currentMonthSpendingLabel;
    }

    private JPanel buildStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLbl.setForeground(COLOR_TEXT_SEC);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // coloured left accent stripe
        JPanel stripe = new JPanel();
        stripe.setBackground(accentColor);
        stripe.setMaximumSize(new Dimension(3, 30));
        stripe.setPreferredSize(new Dimension(3, 30));

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(COLOR_CARD);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(stripe, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.CENTER);

        card.add(Box.createVerticalStrut(6));
        card.add(wrapLeft(titleLbl));
        card.add(Box.createVerticalStrut(6));
        card.add(row);
        card.add(Box.createVerticalStrut(6));
        return card;
    }

    private JPanel createAddExpenseCard() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JLabel title = new JLabel("Add Expense");
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(COLOR_TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(14));

        // Category label + combo
        card.add(fieldLabel("Category"));
        card.add(Box.createVerticalStrut(4));
        categoryBox = new JComboBox<>(new String[]{
            "Food & Beverages", "Entertainment", "Leisure & Sports",
            "Services", "Shopping", "Telecom", "Utilities"
        });
        categoryBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        categoryBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(categoryBox);
        card.add(Box.createVerticalStrut(10));


        // Amount label + field
        card.add(fieldLabel("Amount (RM)"));
        card.add(Box.createVerticalStrut(4));
        amountField = new JTextField();
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(COLOR_BORDER, RADIUS),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        card.add(amountField);
        card.add(Box.createVerticalStrut(14));

        // Add button
        addButton = new JButton("Add Expense");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addButton.setBackground(COLOR_PRIMARY);
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        addButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addButton.addActionListener(this::handleAddExpense);
        card.add(addButton);

        return card;
    }

    // ─────────────────────────────────────────────
    // UPDATE CHARTS
    // ─────────────────────────────────────────────
    private void updateCharts() {
        chartLeftPanel.removeAll();
        chartRightPanel.removeAll();

        try {
            double monthlyTotal = compute.getMonthlyTotal(selectedYear, selectedMonth);
            double avgMonthly   = compute.getAverageMonthlyExpenses(selectedYear);

            currentMonthSpendingLabel.setText(String.format("RM %.2f", monthlyTotal));
            averageExpensesLabel.setText(String.format("RM %.2f", avgMonthly));
            if (currentMonthNameLabel != null) {
                currentMonthNameLabel.setText(monthSelector.getItemAt(selectedMonth - 1) + " " + selectedYear);
            }

            JPanel leftChart, rightChart;

            if (currentViewMode == 1) { // Month view
                Map<Integer, Double> trendData   = compute.getMonthlyTotals(selectedYear);
                Map<String, Double>  monthlyData = compute.getTotalsByCategory(selectedYear, selectedMonth);

                leftChart  = new ChartFrame(applyLightTheme(
                    ChartLine.createMonthlyTrendChart(trendData, selectedYear)));
                rightChart = new ChartFrame(applyLightTheme(
                    ChartPie.createMonthlyPieChart(monthlyData, selectedYear, selectedMonth)));

            } else { // Year view
                Map<String, Double> yearlyData = compute.getTotalsByCategory(selectedYear, null);

                leftChart  = new ChartFrame(applyLightTheme(
                    ChartBar.createCategoryBarChart(yearlyData, "Yearly Overview", "Category", "Amount (RM)")));
                rightChart = new ChartFrame(applyLightTheme(
                    ChartPie.createYearlyPieChart(yearlyData, selectedYear)));
            }

            chartLeftPanel.add(leftChart, BorderLayout.CENTER);
            chartRightPanel.add(rightChart, BorderLayout.CENTER);

        } catch (Exception ex) {
            ex.printStackTrace();
            showChartError("Database connection required. Please ensure accountdb.sql is running.");
        }

        chartLeftPanel.revalidate();
        chartLeftPanel.repaint();
        chartRightPanel.revalidate();
        chartRightPanel.repaint();
    }

    private JFreeChart applyLightTheme(JFreeChart chart) {
        chart.setBackgroundPaint(COLOR_CARD);
        chart.getPlot().setBackgroundPaint(COLOR_CARD);
        if (chart.getTitle() != null)
            chart.getTitle().setPaint(COLOR_TEXT);
        return chart;
    }

    private void showChartError(String msg) {
        JPanel err = new JPanel(new BorderLayout());
        err.setBackground(COLOR_CARD);
        JLabel lbl = new JLabel("<html><center>" + msg + "</center></html>");
        lbl.setForeground(COLOR_TEXT_SEC);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setHorizontalAlignment(JLabel.CENTER);
        err.add(lbl, BorderLayout.CENTER);
        chartLeftPanel.add(err, BorderLayout.CENTER);
        chartLeftPanel.revalidate();
        chartLeftPanel.repaint();
    }

    // ─────────────────────────────────────────────
    // ADD EXPENSE HANDLER
    // ─────────────────────────────────────────────
    private void handleAddExpense(ActionEvent e) {
        try {
            String category = (String) categoryBox.getSelectedItem();
            String amountText = amountField.getText().trim();

            if (amountText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an amount.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double amount = Double.parseDouble(amountText);
            compute.addExpense(selectedYear, selectedMonth, category, amount);
            amountField.setText("");
            updateCharts();
            JOptionPane.showMessageDialog(this, "Expense added successfully!",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.",
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────
    /** White rounded card panel */
    private JPanel createCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(COLOR_BORDER, RADIUS),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        return card;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(COLOR_TEXT_SEC);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel wrapLeft(JComponent comp) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(COLOR_CARD);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(comp);
        return p;
    }

    // ─────────────────────────────────────────────
    // ROUNDED BORDER  (helper inner class)
    // ─────────────────────────────────────────────
    static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        RoundedBorder(Color color, int radius) {
            this.color  = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.draw(new RoundRectangle2D.Double(x, y, w - 1, h - 1, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(radius / 2, radius / 2, radius / 2, radius / 2); }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.set(radius / 2, radius / 2, radius / 2, radius / 2);
            return insets;
        }
    }

    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpensesComputeApplication::new);
    }
}