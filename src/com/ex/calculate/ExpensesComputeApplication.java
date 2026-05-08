package com.ex.calculate;

import chart.ChartFrame;
import chart.ChartPie;
import chart.SQLConnection;
import chart.ChartBar;
import chart.ChartLine;

import org.jfree.chart.JFreeChart;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.Map;

public class ExpensesComputeApplication extends JFrame {

    // ── fields ───────────────────────────────────────────────────────────────
    private JPanel   chartLeftPanel, chartRightPanel, categorySummaryPanel;
    private JLabel   currentMonthSpendingLabel, averageExpensesLabel,
                     yearlyTotalLabel, monthChangeLabel;
    private JButton  tabButtonMonth, tabButtonYear;
    private JComboBox<String> monthSelector;
    private int      currentViewMode = 1;

    private JLabel monthChangeIconLabel;
    private JPanel monthChangeIconCircle;
    private Color  monthChangeAccent;

    private SwingWorker<Void,Void> pendingWorker;
    private ExpensesCompute compute;
    private int selectedYear  = LocalDate.now().getYear();
    private int selectedMonth = LocalDate.now().getMonthValue();

    // ── macOS Sonoma color tokens ────────────────────────────────────────────
    private static final Color BG        = new Color(0xF6F6F6);
    private static final Color SIDEBAR_BG= new Color(0xF6F6F6);
    private static final Color CARD_BG   = Color.WHITE;
    private static final Color TOOLBAR_BG= new Color(0xF6F6F6);
    private static final Color SEP       = new Color(0xC6C6C8);
    private static final Color SEP_LIGHT = new Color(0xE5E5EA);
    private static final Color LABEL     = new Color(0x1C1C1E);
    private static final Color LABEL_2   = new Color(0x6E6E73);
    private static final Color LABEL_3   = new Color(0xAEAEB2);
    private static final Color ACCENT    = new Color(0x007AFF);
    private static final Color INDIGO    = new Color(0x5856D6);
    private static final Color GREEN     = new Color(0x34C759);
    private static final Color RED       = new Color(0xFF3B30);
    private static final Color ORANGE    = new Color(0xFF9500);

    private static final Color[] CAT_COLORS = {
        ACCENT, GREEN, ORANGE, RED, INDIGO,
        new Color(0xFF2D55), new Color(0x30B0C7)
    };

    private static final int R         = 10;
    private static final int SIDEBAR_W = 220;

    private static Font sf(int style, float size) {
        for (String n : new String[]{".SF NS Display",".SF NS Text","Helvetica Neue","Helvetica","SansSerif"}) {
            Font f = new Font(n, style, (int) size);
            if (!f.getFamily().equals("Dialog")) return f.deriveFont(size);
        }
        return new Font("SansSerif", style, (int) size);
    }

    // ════════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ════════════════════════════════════════════════════════════════════════
    public ExpensesComputeApplication() {
        super("Account Dashboard");
        compute = new ExpensesCompute();

        try {
            UIManager.setLookAndFeel(new FlatMacLightLaf());
            UIManager.put("Component.focusWidth", 0);
            UIManager.put("Button.arc",           R);
            UIManager.put("TextComponent.arc",    R);
            UIManager.put("ComboBox.arc",         R);
            UIManager.put("Panel.background",     BG);
            UIManager.put("ScrollBar.width",      7);
            UIManager.put("ScrollBar.thumbArc",   999);
        } catch (Exception e) { e.printStackTrace(); }

        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(buildTitleBar(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(BG);
        body.add(buildSidebar(),     BorderLayout.WEST);
        body.add(buildMainCanvas(),  BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        setSize(1340, 840);
        setMinimumSize(new Dimension(980, 640));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        SwingUtilities.invokeLater(this::updateCharts);
    }

    // ════════════════════════════════════════════════════════════════════════
    // TITLE BAR
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(TOOLBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(SEP);
                g2.fillRect(0, getHeight()-1, getWidth(), 1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 50));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(SIDEBAR_W, 50));
        left.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        JLabel title = new JLabel("Account Dashboard");
        title.setFont(sf(Font.BOLD, 13f));
        title.setForeground(LABEL);
        left.add(Box.createHorizontalStrut(10));
        left.add(title);
        bar.add(left, BorderLayout.WEST);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 9));
        center.setOpaque(false);
        center.add(buildSegmentedControl());
        center.add(thinRule(1, 24));
        center.add(smallLabel("Month"));
        monthSelector = new JComboBox<>(new String[]{
            "January","February","March","April","May","June",
            "July","August","September","October","November","December"});
        monthSelector.setSelectedIndex(selectedMonth - 1);
        monthSelector.setFont(sf(Font.PLAIN, 12f));
        monthSelector.setPreferredSize(new Dimension(118, 28));
        monthSelector.addActionListener(e -> { selectedMonth = monthSelector.getSelectedIndex()+1; updateCharts(); });
        center.add(monthSelector);
        center.add(thinRule(1, 24));
        center.add(smallLabel("Year"));
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(selectedYear, 2000, 2099, 1));
        yearSpinner.setFont(sf(Font.PLAIN, 12f));
        yearSpinner.setPreferredSize(new Dimension(72, 28));
        yearSpinner.addChangeListener(e -> { selectedYear = (int) yearSpinner.getValue(); updateCharts(); });
        center.add(yearSpinner);
        bar.add(center, BorderLayout.CENTER);

        return bar;
    }

    // ════════════════════════════════════════════════════════════════════════
    // SEGMENTED CONTROL
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildSegmentedControl() {
        JPanel seg = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xDEDEE3));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                g2.dispose();
            }
        };
        seg.setOpaque(false);
        seg.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        tabButtonMonth = buildSegBtn("Monthly", true);
        tabButtonYear  = buildSegBtn("Yearly",  false);
        seg.add(tabButtonMonth);
        seg.add(tabButtonYear);
        return seg;
    }

    private JButton buildSegBtn(String text, boolean active) {
        JButton btn = new JButton(text) {
            boolean isActive() { return getBackground() == CARD_BG; }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isActive()) {
                    g2.setColor(new Color(0,0,0,25));
                    g2.fillRoundRect(1, 2, getWidth(), getHeight(), 7, 7);
                    g2.setColor(CARD_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                }
                g2.setFont(getFont()); g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(sf(active ? Font.BOLD : Font.PLAIN, 12.5f));
        btn.setPreferredSize(new Dimension(78, 24));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBackground(active ? CARD_BG : new Color(0,0,0,0));
        btn.setForeground(active ? LABEL : LABEL_2);
        btn.addActionListener(e -> {
            activateSeg(btn);
            currentViewMode = text.equals("Monthly") ? 1 : 2;
            updateCharts();
        });
        return btn;
    }

    private void activateSeg(JButton active) {
        for (JButton b : new JButton[]{tabButtonMonth, tabButtonYear}) {
            if (b == null) continue;
            b.setBackground(new Color(0,0,0,0));
            b.setForeground(LABEL_2);
            b.setFont(sf(Font.PLAIN, 12.5f));
        }
        active.setBackground(CARD_BG);
        active.setForeground(LABEL);
        active.setFont(sf(Font.BOLD, 12.5f));
        tabButtonMonth.repaint(); tabButtonYear.repaint();
    }

    // ════════════════════════════════════════════════════════════════════════
    // SIDEBAR  (Overview only — Database/Settings pages removed)
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(SIDEBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(SEP);
                g2.fillRect(getWidth()-1, 0, 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_W, 0));

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        nav.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        nav.add(sidebarSectionLabel("MENU"));
        nav.add(Box.createVerticalStrut(4));
        nav.add(buildNavItem("⊞", "Overview", true, null));
        sidebar.add(nav, BorderLayout.NORTH);

        return sidebar;
    }

    private JPanel sidebarSectionLabel(String text) {
        JPanel w = new JPanel(new BorderLayout());
        w.setOpaque(false);
        w.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        w.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        JLabel lbl = new JLabel(text);
        lbl.setFont(sf(Font.BOLD, 10f));
        lbl.setForeground(LABEL_3);
        w.add(lbl, BorderLayout.WEST);
        return w;
    }

    private JPanel buildNavItem(String icon, String label, boolean active, Runnable onClick) {
        JPanel row = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                boolean isActive = Boolean.TRUE.equals(getClientProperty("navActive"));
                boolean hover    = Boolean.TRUE.equals(getClientProperty("hover"));
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isActive) {
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(6, 3, getWidth()-12, getHeight()-6, 8, 8);
                } else if (hover) {
                    g2.setColor(new Color(0,0,0,18));
                    g2.fillRoundRect(6, 3, getWidth()-12, getHeight()-6, 8, 8);
                }
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        row.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.putClientProperty("navActive", active);

        JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        inner.setOpaque(false);
        JLabel ico = new JLabel(icon);
        ico.setFont(sf(Font.PLAIN, 14f));
        ico.setPreferredSize(new Dimension(18, 18));
        ico.setForeground(active ? Color.WHITE : LABEL_2);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIManager.getFont(active ? "h3.font" : "h3.regular.font"));
        lbl.setForeground(active ? Color.WHITE : LABEL);
        inner.add(ico); inner.add(lbl);
        row.add(inner, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { row.putClientProperty("hover", true);  row.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { row.putClientProperty("hover", false); row.repaint(); }
            @Override public void mouseClicked(MouseEvent e) { if (onClick != null) onClick.run(); }
        });
        return row;
    }

    // ════════════════════════════════════════════════════════════════════════
    // MAIN CANVAS
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildMainCanvas() {
        JPanel canvas = new JPanel(new BorderLayout());
        canvas.setBackground(BG);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(BG);
        inner.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        statsRow.add(buildStatCard("Yearly Total",  yearlyTotalLabelRef(),         ACCENT,  "◈"));
        statsRow.add(buildStatCard("vs Last Month", monthChangeLabelRef(),          RED,     "↑"));
        statsRow.add(buildStatCard("Avg / Month",   averageExpensesLabelRef(),      INDIGO,  "⌀"));
        statsRow.add(buildStatCard("This Month",    currentMonthSpendingLabelRef(), ORANGE,  "●"));
        inner.add(statsRow);
        inner.add(Box.createVerticalStrut(14));

        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 12, 0));
        chartsRow.setOpaque(false);

        chartLeftPanel = createCard();
        chartLeftPanel.setLayout(new BorderLayout());
        chartLeftPanel.setPreferredSize(new Dimension(0, 350));
        chartLeftPanel.setMinimumSize(new Dimension(180, 240));
        chartsRow.add(chartLeftPanel);

        chartRightPanel = createCard();
        chartRightPanel.setLayout(new BorderLayout());
        chartRightPanel.setPreferredSize(new Dimension(0, 350));
        chartRightPanel.setMinimumSize(new Dimension(180, 240));
        chartsRow.add(chartRightPanel);

        inner.add(chartsRow);
        inner.add(Box.createVerticalStrut(14));
        inner.add(buildCategoryBreakdownCard());
        inner.add(Box.createVerticalGlue());

        JScrollPane sp = new JScrollPane(inner);
        sp.setBorder(null);
        sp.setBackground(BG);
        sp.getViewport().setBackground(BG);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(sp.getVerticalScrollBar());
        canvas.add(sp, BorderLayout.CENTER);
        return canvas;
    }

    // ════════════════════════════════════════════════════════════════════════
    // STAT CARD
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildStatCard(String title, JLabel valueLabel, Color accent, String icon) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(14, 0));

        JPanel iconCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = (monthChangeIconCircle == this && monthChangeAccent != null) ? monthChangeAccent : accent;
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(34, 34); }
        };
        iconCircle.setOpaque(false);
        iconCircle.setLayout(new GridBagLayout());

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(sf(Font.PLAIN, 15f));
        iconLabel.setForeground(accent);
        iconCircle.add(iconLabel);

        if (title.equals("vs Last Month")) {
            monthChangeIconLabel  = iconLabel;
            monthChangeIconCircle = iconCircle;
            monthChangeAccent     = accent;
        }

        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(iconCircle);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(sf(Font.PLAIN, 11.5f));
        titleLbl.setForeground(LABEL_2);
        valueLabel.setFont(UIManager.getFont("h3.font"));
        valueLabel.setForeground(LABEL);

        text.add(titleLbl);
        text.add(Box.createVerticalStrut(3));
        text.add(valueLabel);

        card.add(iconWrap, BorderLayout.WEST);
        card.add(text,     BorderLayout.CENTER);
        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    // CATEGORY BREAKDOWN
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildCategoryBreakdownCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        JLabel title = new JLabel("Yearly Breakdown");
        title.setFont(UIManager.getFont("h3.font"));
        title.setForeground(LABEL);
        card.add(title, BorderLayout.NORTH);
        categorySummaryPanel = new JPanel(new GridLayout(0, 4, 10, 8));
        categorySummaryPanel.setBackground(CARD_BG);
        card.add(categorySummaryPanel, BorderLayout.CENTER);
        return card;
    }

    private void updateCategoryBreakdown(Map<String, Double> totals) {
        if (categorySummaryPanel == null) return;
        categorySummaryPanel.removeAll();
        if (totals == null || totals.isEmpty()) {
            JLabel e = new JLabel(totals == null ? "No database connection." : "No data for " + selectedYear);
            e.setFont(sf(Font.PLAIN, 13f));
            e.setForeground(LABEL_3);
            categorySummaryPanel.add(e);
        } else {
            int ci = 0;
            for (Map.Entry<String, Double> entry : totals.entrySet()) {
                categorySummaryPanel.add(buildCategoryChip(entry.getKey(), entry.getValue(), CAT_COLORS[ci++ % CAT_COLORS.length]));
            }
        }
        categorySummaryPanel.revalidate();
        categorySummaryPanel.repaint();
    }

    private JPanel buildCategoryChip(String category, double amount, Color accent) {
        JPanel chip = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 14));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setBorder(BorderFactory.createCompoundBorder(
            new MacBorder(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 55), 10),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, (getHeight()-8)/2, 8, 8);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(10, 10); }
        };
        dot.setOpaque(false);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);
        JLabel catLbl = new JLabel(category);
        catLbl.setFont(sf(Font.PLAIN, 11.5f));
        catLbl.setForeground(LABEL_2);
        JLabel amtLbl = new JLabel(String.format("RM %.2f", amount));
        amtLbl.setFont(UIManager.getFont("h3.font"));
        amtLbl.setForeground(LABEL);
        textCol.add(catLbl);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(amtLbl);

        chip.add(dot,     BorderLayout.WEST);
        chip.add(textCol, BorderLayout.CENTER);
        return chip;
    }

    // ════════════════════════════════════════════════════════════════════════
    // UPDATE CHARTS
    // ════════════════════════════════════════════════════════════════════════
    public void updateCharts() {
        if (pendingWorker != null && !pendingWorker.isDone()) pendingWorker.cancel(false);

        final int year  = selectedYear;
        final int month = selectedMonth;
        final int mode  = currentViewMode;

        pendingWorker = new SwingWorker<Void, Void>() {
            private boolean dbOk;
            private double  monthlyTotal, avgMonthly, yearlyTotal, monthChange;
            private JFreeChart leftChart, rightChart;
            private Map<String, Double> catTotals;

            @Override
            protected Void doInBackground() {
                try {
                    Connection conn = SQLConnection.getInstance().getConnection();
                    if (conn == null || conn.isClosed()) return null;

                    monthlyTotal = compute.getMonthlyTotal(year, month);
                    avgMonthly   = compute.getAverageMonthlyExpenses(year);
                    yearlyTotal  = compute.getYearlyTotal(year);
                    monthChange  = compute.getMonthComparison(year, month - 1, month);
                    catTotals    = compute.getTotalsByCategory(year, null);

                    if (mode == 1) {
                        Map<Integer, Double> trend   = compute.getMonthlyTotals(year);
                        Map<String, Double>  monthly = compute.getTotalsByCategory(year, month);
                        leftChart  = theme(ChartPie.createMonthlyPieChart(monthly, year, month));
                        rightChart = theme(ChartLine.createMonthlyTrendChart(trend, year));
                    } else {
                        Map<String, Double> yearly = compute.getTotalsByCategory(year, null);
                        leftChart  = theme(ChartPie.createYearlyPieChart(yearly, year));
                        rightChart = theme(ChartBar.createCategoryBarChart(yearly, "Yearly Overview", "Category", "Amount (RM)"));
                    }
                    dbOk = true;
                } catch (Exception ex) { ex.printStackTrace(); }
                return null;
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                if (!dbOk) {
                    currentMonthSpendingLabel.setText("—");
                    averageExpensesLabel.setText("—");
                    yearlyTotalLabel.setText("—");
                    monthChangeLabel.setText("—");
                    monthChangeLabel.setForeground(LABEL_2);
                    if (monthChangeIconLabel != null) {
                        monthChangeIconLabel.setText("—");
                        monthChangeIconLabel.setForeground(LABEL_2);
                        monthChangeAccent = LABEL_2;
                        monthChangeIconCircle.repaint();
                    }
                    chartLeftPanel.removeAll();
                    chartRightPanel.removeAll();
                    showNoDbPlaceholder();
                    updateCategoryBreakdown(null);
                } else {
                    currentMonthSpendingLabel.setText(String.format("RM %.2f", monthlyTotal));
                    averageExpensesLabel.setText(String.format("RM %.2f", avgMonthly));
                    yearlyTotalLabel.setText(String.format("RM %.2f", yearlyTotal));
                    monthChangeLabel.setText(monthChange != 0 ? String.format("%+.1f%%", monthChange) : "—");

                    if (monthChange < 0) {
                        monthChangeLabel.setForeground(GREEN);
                        monthChangeIconLabel.setText("↓"); monthChangeIconLabel.setForeground(GREEN);
                        monthChangeAccent = GREEN;
                    } else if (monthChange > 0) {
                        monthChangeLabel.setForeground(RED);
                        monthChangeIconLabel.setText("↑"); monthChangeIconLabel.setForeground(RED);
                        monthChangeAccent = RED;
                    } else {
                        monthChangeLabel.setForeground(LABEL_2);
                        monthChangeIconLabel.setText("—"); monthChangeIconLabel.setForeground(LABEL_2);
                        monthChangeAccent = LABEL_2;
                    }
                    monthChangeIconCircle.repaint();

                    chartLeftPanel.removeAll();
                    chartRightPanel.removeAll();
                    ChartFrame lf = new ChartFrame(leftChart);
                    lf.setPreferredSize(new Dimension(340, 320));
                    ChartFrame rf = new ChartFrame(rightChart);
                    rf.setPreferredSize(new Dimension(400, 320));
                    chartLeftPanel.add(lf, BorderLayout.CENTER);
                    chartRightPanel.add(rf, BorderLayout.CENTER);
                    updateCategoryBreakdown(catTotals);
                }
                chartLeftPanel.revalidate();  chartLeftPanel.repaint();
                chartRightPanel.revalidate(); chartRightPanel.repaint();
            }
        };
        pendingWorker.execute();
    }

    private void showNoDbPlaceholder() {
        JLabel err = new JLabel("Database connection required.");
        err.setForeground(LABEL_3);
        err.setFont(sf(Font.PLAIN, 12f));
        err.setHorizontalAlignment(JLabel.CENTER);
        chartLeftPanel.add(err, BorderLayout.CENTER);
    }

    private JFreeChart theme(JFreeChart chart) {
        chart.setBackgroundPaint(CARD_BG);
        chart.getPlot().setBackgroundPaint(CARD_BG);
        if (chart.getTitle() != null) {
            chart.getTitle().setPaint(LABEL);
            chart.getTitle().setFont(UIManager.getFont("h3.font"));
        }
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(CARD_BG);
            chart.getLegend().setItemPaint(LABEL_2);
            chart.getLegend().setItemFont(sf(Font.PLAIN, 11f));
        }
        if (chart.getPlot() instanceof org.jfree.chart.plot.CategoryPlot) {
            org.jfree.chart.plot.CategoryPlot p = (org.jfree.chart.plot.CategoryPlot) chart.getPlot();
            p.setRangeGridlinePaint(SEP_LIGHT);
            p.getDomainAxis().setTickLabelPaint(LABEL_2); p.getDomainAxis().setLabelPaint(LABEL_2);
            p.getDomainAxis().setAxisLinePaint(SEP_LIGHT); p.getDomainAxis().setTickLabelFont(sf(Font.PLAIN, 10f));
            p.getRangeAxis().setTickLabelPaint(LABEL_2);  p.getRangeAxis().setLabelPaint(LABEL_2);
            p.getRangeAxis().setAxisLinePaint(SEP_LIGHT);  p.getRangeAxis().setTickLabelFont(sf(Font.PLAIN, 10f));
        }
        return chart;
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════════
    private JPanel createCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 5; i >= 1; i--) {
                    g2.setColor(new Color(0,0,0,5));
                    g2.fillRoundRect(i, i+1, getWidth()-i*2, getHeight()-i*2, R+i, R+i);
                }
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, R, R);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            new MacBorder(SEP_LIGHT, R),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return card;
    }

    private JLabel yearlyTotalLabelRef()          { yearlyTotalLabel         = new JLabel("RM 0.00"); return yearlyTotalLabel; }
    private JLabel monthChangeLabelRef()           { monthChangeLabel         = new JLabel("—");       return monthChangeLabel; }
    private JLabel averageExpensesLabelRef()       { averageExpensesLabel     = new JLabel("RM 0.00"); return averageExpensesLabel; }
    private JLabel currentMonthSpendingLabelRef()  { currentMonthSpendingLabel= new JLabel("RM 0.00"); return currentMonthSpendingLabel; }

    private JLabel smallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(sf(Font.PLAIN, 12f)); l.setForeground(LABEL_2);
        return l;
    }

    private Component thinRule(int w, int h) {
        JPanel r = new JPanel();
        r.setBackground(SEP);
        r.setPreferredSize(new Dimension(w, h));
        r.setMaximumSize(new Dimension(w, h));
        return r;
    }

    private void styleScrollBar(JScrollBar bar) {
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() { thumbColor = new Color(0xC6C6C8); trackColor = BG; }
            @Override protected JButton createDecreaseButton(int o) { return zero(); }
            @Override protected JButton createIncreaseButton(int o) { return zero(); }
            JButton zero() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // MacBorder
    // ════════════════════════════════════════════════════════════════════════
    static class MacBorder extends AbstractBorder {
        private final Color color; private final int radius;
        MacBorder(Color c, int r) { color = c; radius = r; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.draw(new RoundRectangle2D.Double(x+.5, y+.5, w-1, h-1, radius, radius));
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c)           { return new Insets(1,1,1,1); }
        @Override public Insets getBorderInsets(Component c, Insets i) { i.set(1,1,1,1); return i; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpensesComputeApplication::new);
    }
}
