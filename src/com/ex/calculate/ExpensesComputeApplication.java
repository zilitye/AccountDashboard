package com.ex.calculate;

import chart.ChartFrame;
import chart.ChartPie;
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
import java.time.LocalDate;
import java.util.Map;

/**
 * Account Dashboard — macOS Sonoma / Sequoia style.
 *
 * Layout:
 *   ┌──────────────────────────────────────────────────────┐
 *   │  Title bar  (traffic lights · title · toolbar)       │
 *   ├─────────────┬────────────────────────────────────────┤
 *   │             │  stat cards                            │
 *   │  Sidebar    │  charts (2-col)                        │
 *   │  ─ nav      │  category breakdown                    │
 *   │  ─ (glue)   │                                        │
 *   │  ─ ⓘ About  │                                        │
 *   └─────────────┴────────────────────────────────────────┘
 */
public class ExpensesComputeApplication extends JFrame {

    // ── fields ───────────────────────────────────────────────────────────────
    private JPanel   chartLeftPanel, chartRightPanel;
    private JPanel   categorySummaryPanel;
    private JLabel   currentMonthSpendingLabel, averageExpensesLabel,
                     yearlyTotalLabel, monthChangeLabel;
    private JButton  tabButtonMonth, tabButtonYear;
    private JComboBox<String> monthSelector;
    private int      currentViewMode = 1;

    private JLabel monthChangeIconLabel;
    private JPanel monthChangeIconCircle;
    private Color monthChangeAccent;

    // Add-expense dialog fields (created fresh each time)
    private JComboBox<String> dlgCategoryBox;
    private JTextField        dlgAmountField;
    private JComboBox<String> dlgMonthBox;
    private JSpinner          dlgYearSpinner;

    private ExpensesCompute compute;
    private int selectedYear  = LocalDate.now().getYear();
    private int selectedMonth = LocalDate.now().getMonthValue();

    // ── macOS Sonoma color tokens ────────────────────────────────────────────
    private static final Color BG           = new Color(0xf6f6f6);   // systemGroupedBackground
    private static final Color SIDEBAR_BG   = new Color(0xf6f6f6);   // sidebar vibrancy simulation
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color TOOLBAR_BG   = new Color(0xf6f6f6);

    private static final Color SEP          = new Color(0xC6C6C8);   // opaqueSeparator
    private static final Color SEP_LIGHT    = new Color(0xE5E5EA);

    private static final Color LABEL        = new Color(0x1C1C1E);   // label
    private static final Color LABEL_2      = new Color(0x6E6E73);   // secondaryLabel
    private static final Color LABEL_3      = new Color(0xAEAEB2);   // tertiaryLabel
    private static final Color LABEL_4      = new Color(0xC7C7CC);   // quaternaryLabel

    private static final Color ACCENT       = new Color(0x007AFF);   // systemBlue
    private static final Color INDIGO       = new Color(0x5856D6);   // systemIndigo
    private static final Color GREEN        = new Color(0x34C759);   // systemGreen
    private static final Color RED          = new Color(0xFF3B30);   // systemRed
    private static final Color ORANGE       = new Color(0xFF9500);   // systemOrange

    private static final Color[] CAT_COLORS = {
        ACCENT, GREEN, ORANGE, RED, INDIGO,
        new Color(0xFF2D55), new Color(0x30B0C7)
    };

    private static final int R          = 10;   // standard card radius
    private static final int SIDEBAR_W  = 220;

    // ── font helper ──────────────────────────────────────────────────────────
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
            UIManager.put("Component.focusWidth",  0);
            UIManager.put("Button.arc",            R);
            UIManager.put("TextComponent.arc",     R);
            UIManager.put("ComboBox.arc",          R);
            UIManager.put("Panel.background",      BG);
            UIManager.put("ScrollBar.width",       7);
            UIManager.put("ScrollBar.thumbArc",    999);
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
        // Full-width painted panel
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(TOOLBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(SEP);
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 50));

        // ── LEFT: traffic lights + app title (aligned with sidebar) ──────────
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(SIDEBAR_W, 50));
        left.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));

        // Traffic-light circles
        JPanel lights = new JPanel(null) {
            @Override public Dimension getPreferredSize() { return new Dimension(52, 50); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color[] c = {new Color(0xFF5F57), new Color(0xFEBC2E), new Color(0x28C840)};
                for (int i = 0; i < 3; i++) {
                    int x = i * 18;
                    int y = (getHeight() - 12) / 2;
                    g2.setColor(c[i]);
                    g2.fillOval(x, y, 12, 12);
                    g2.setColor(new Color(255,255,255,55));
                    g2.fillOval(x+2, y+1, 5, 4);
                }
                g2.dispose();
            }
        };
        lights.setOpaque(false);

        JLabel title = new JLabel("Account Dashboard");
        title.setFont(sf(Font.BOLD, 13f));
        title.setForeground(LABEL);

        left.add(lights);
        left.add(Box.createHorizontalStrut(10));
        left.add(title);
        bar.add(left, BorderLayout.WEST);

        // ── CENTER: segmented control + month/year pickers ────────────────────
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 9));
        center.setOpaque(false);

        center.add(buildSegmentedControl());
        center.add(thinRule(1, 24));

        center.add(smallLabel("Month"));
        String[] months = {"January","February","March","April","May","June",
                           "July","August","September","October","November","December"};
        monthSelector = new JComboBox<>(months);
        monthSelector.setSelectedIndex(selectedMonth - 1);
        monthSelector.setFont(sf(Font.PLAIN, 12f));
        monthSelector.setPreferredSize(new Dimension(118, 28));
        monthSelector.addActionListener(e -> { selectedMonth = monthSelector.getSelectedIndex()+1; updateCharts(); });
        center.add(monthSelector);

        center.add(thinRule(1, 24));
        center.add(smallLabel("Year"));

        SpinnerNumberModel ym = new SpinnerNumberModel(selectedYear, 2000, 2099, 1);
        JSpinner yearSpinner = new JSpinner(ym);
        yearSpinner.setFont(sf(Font.PLAIN, 12f));
        yearSpinner.setPreferredSize(new Dimension(72, 28));
        yearSpinner.addChangeListener(e -> { selectedYear = (int) yearSpinner.getValue(); updateCharts(); });
        center.add(yearSpinner);

        bar.add(center, BorderLayout.CENTER);

        // ── RIGHT: "+ Add Expense" button ─────────────────────────────────────
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 9));
        right.setOpaque(false);

        JButton addBtn = buildToolbarAddButton();
        right.add(addBtn);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    /** The blue "+ Add Expense" toolbar button */
    private JButton buildToolbarAddButton() {
        JButton btn = new JButton("+ Add Expense") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(0x0065D9) : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(sf(Font.PLAIN, 13f));
        btn.setPreferredSize(new Dimension(130, 30));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showAddExpenseDialog());
        return btn;
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
                    // white pill with subtle drop shadow
                    g2.setColor(new Color(0,0,0,25));
                    g2.fillRoundRect(1, 2, getWidth(), getHeight(), 7, 7);
                    g2.setColor(CARD_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                }
                g2.setFont(getFont());
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(sf(active ? Font.BOLD : Font.PLAIN, 12.5f));
        btn.setPreferredSize(new Dimension(78, 24));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
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
        tabButtonMonth.repaint();
        tabButtonYear.repaint();
    }

    // ════════════════════════════════════════════════════════════════════════
    // SIDEBAR
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(SIDEBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // right hairline
                g2.setColor(SEP);
                g2.fillRect(getWidth()-1, 0, 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_W, 0));

        // ── Top: nav items ────────────────────────────────────────────────────
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        nav.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        // Section label: "MENU"
        nav.add(sidebarSectionLabel("MENU"));
        nav.add(Box.createVerticalStrut(4));

        String[][] items = {
            {"⊞", "Overview"},
            {"◫", "Database"},
            {"⚙", "Settings"},
        };
        for (int i = 0; i < items.length; i++) {
            nav.add(buildNavItem(items[i][0], items[i][1], i == 0));
        }

        sidebar.add(nav, BorderLayout.NORTH);

        // ── Bottom: ⓘ About ───────────────────────────────────────────────────
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        // hairline divider
        bottom.add(buildSidebarDivider());
        bottom.add(Box.createVerticalStrut(6));
        bottom.add(buildNavItem("ⓘ", "About", false, () -> showAboutDialog()));

        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel sidebarSectionLabel(String text) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel lbl = new JLabel(text);
        lbl.setFont(sf(Font.BOLD, 10f));
        lbl.setForeground(LABEL_3);

        // SAME left inset as nav items
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        wrapper.add(lbl, BorderLayout.WEST); // stick to left properly
        return wrapper;
    }

    /** Nav item without custom click action (no-op hover only) */
    private JPanel buildNavItem(String icon, String label, boolean active) {
        return buildNavItem(icon, label, active, null);
    }

    /** Nav item with optional click action */
    private JPanel buildNavItem(String icon, String label, boolean active, Runnable onClick) {
        JPanel row = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (active) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // blue selection pill
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(6, 3, getWidth()-12, getHeight()-6, 8, 8);
                    g2.dispose();
                }
            }
        };
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        row.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // icon + label in a horizontal inner panel
        JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        inner.setOpaque(false);
        inner.setPreferredSize(new Dimension(SIDEBAR_W - 16, 34));

        JLabel ico = new JLabel(icon);
        ico.setFont(sf(Font.PLAIN, 14f));
        ico.setForeground(active ? Color.WHITE : LABEL_2);
        ico.setPreferredSize(new Dimension(18, 18));

        JLabel lbl = new JLabel(label);
        lbl.setFont(active? UIManager.getFont(	"h3.font"): UIManager.getFont("h3.regular.font"));
        lbl.setForeground(active ? Color.WHITE : LABEL);

        inner.add(ico);
        inner.add(lbl);
        row.add(inner, BorderLayout.CENTER);

        if (!active) {
            row.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    row.setOpaque(true);
                    row.setBackground(new Color(0,0,0,0));
                    // draw a subtle hover pill
                    row.putClientProperty("hover", true);
                    row.repaint();
                }
                @Override public void mouseExited(MouseEvent e) {
                    row.setOpaque(false);
                    row.putClientProperty("hover", false);
                    row.repaint();
                }
                @Override public void mouseClicked(MouseEvent e) {
                    if (onClick != null) onClick.run();
                }
            });

            // override paint to show hover state
            // We replace with a version that reads "hover" property
        }

        if (!active) {
            // Replace the component with one that can show hover
            JPanel hoverRow = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Object h = getClientProperty("hover");
                    if (Boolean.TRUE.equals(h)) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(0,0,0,18));
                        g2.fillRoundRect(6, 3, getWidth()-12, getHeight()-6, 8, 8);
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            };
            hoverRow.setOpaque(false);
            hoverRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            hoverRow.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            hoverRow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            hoverRow.add(inner, BorderLayout.CENTER);
            hoverRow.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hoverRow.putClientProperty("hover",true); hoverRow.repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hoverRow.putClientProperty("hover",false); hoverRow.repaint(); }
                @Override public void mouseClicked(MouseEvent e) { if (onClick != null) onClick.run(); }
            });
            return hoverRow;
        }

        return row;
    }

    private Component buildSidebarDivider() {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        wrap.setOpaque(false);
        JPanel line = new JPanel();
        line.setBackground(SEP_LIGHT);
        line.setOpaque(true);
        line.setPreferredSize(new Dimension(SIDEBAR_W - 24, 1));
        line.setMaximumSize(new Dimension(SIDEBAR_W - 24, 1));
        wrap.add(line);
        return wrap;
    }

    // ════════════════════════════════════════════════════════════════════════
    // ABOUT DIALOG
    // ════════════════════════════════════════════════════════════════════════
    private void showAboutDialog() {
        JDialog dlg = new JDialog(this, "About Account Dashboard", true);
        dlg.setSize(400, 300);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createEmptyBorder(32, 36, 28, 36));

        // App icon placeholder
        JPanel iconCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x007AFF22, true));
                g2.fillOval(0, 0, 56, 56);
                g2.setColor(ACCENT);
                g2.setFont(sf(Font.PLAIN, 26f));
                FontMetrics fm = g2.getFontMetrics();
                String ic = "◈";
                g2.drawString(ic, (56 - fm.stringWidth(ic))/2, (56 + fm.getAscent() - fm.getDescent())/2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(56,56); }
        };
        iconCircle.setOpaque(false);
        iconCircle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("Account Dashboard");
        appName.setFont(sf(Font.BOLD, 17f));
        appName.setForeground(LABEL);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel version = new JLabel("Version 1.0.0");
        version.setFont(sf(Font.PLAIN, 12f));
        version.setForeground(LABEL_2);
        version.setAlignmentX(Component.CENTER_ALIGNMENT);

        // hairline divider
        JSeparator sep = new JSeparator();
        sep.setForeground(SEP_LIGHT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JLabel credit = new JLabel("<html><center>Created by <b>Tye Zi Li / UPM</b></center></html>");
        credit.setFont(sf(Font.PLAIN, 13f));
        credit.setForeground(LABEL);
        credit.setAlignmentX(Component.CENTER_ALIGNMENT);

        String disclaimerText = "<html><center>"
            + "This application is developed for academic and personal budgeting purposes. "
            + "All financial data is stored locally. No data is transmitted or shared externally."
            + "</center></html>";
        JLabel disclaimer = new JLabel(disclaimerText);
        disclaimer.setFont(sf(Font.PLAIN, 11.5f));
        disclaimer.setForeground(LABEL_2);
        disclaimer.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton closeBtn = buildBlueButton("OK");
        closeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeBtn.setMaximumSize(new Dimension(80, 30));
        closeBtn.addActionListener(e -> dlg.dispose());

        root.add(iconCircle);
        root.add(Box.createVerticalStrut(12));
        root.add(appName);
        root.add(Box.createVerticalStrut(4));
        root.add(version);
        root.add(Box.createVerticalStrut(18));
        root.add(sep);
        root.add(Box.createVerticalStrut(14));
        root.add(credit);
        root.add(Box.createVerticalStrut(8));
        root.add(disclaimer);
        root.add(Box.createVerticalStrut(20));
        root.add(closeBtn);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    // ADD EXPENSE DIALOG  (shown by toolbar button)
    // ════════════════════════════════════════════════════════════════════════
    private void showAddExpenseDialog() {
        JDialog dlg = new JDialog(this, "Add Expense", true);
        dlg.setSize(380, 345);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(CARD_BG);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 20, 28));

        JLabel heading = new JLabel("New Expense");
        heading.setFont(sf(Font.BOLD, 16f));
        heading.setForeground(LABEL);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(heading);
        root.add(Box.createVerticalStrut(20));

        // Category row
        root.add(formLabel("Category"));
        root.add(Box.createVerticalStrut(4));
        dlgCategoryBox = new JComboBox<>(new String[]{
            "Food & Beverages","Entertainment","Leisure & Sports",
            "Services","Shopping","Telecom","Utilities"
        });
        dlgCategoryBox.setFont(sf(Font.PLAIN, 13f));
        dlgCategoryBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        dlgCategoryBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(dlgCategoryBox);
        root.add(Box.createVerticalStrut(14));

        // Amount row
        root.add(formLabel("Amount (RM)"));
        root.add(Box.createVerticalStrut(4));
        dlgAmountField = new JTextField();
        dlgAmountField.setFont(sf(Font.PLAIN, 13f));
        dlgAmountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        dlgAmountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        dlgAmountField.setBackground(new Color(0xF9F9FB));
        dlgAmountField.setBorder(BorderFactory.createCompoundBorder(
            new MacBorder(SEP, 8),
            BorderFactory.createEmptyBorder(5, 9, 5, 9)
        ));
        root.add(dlgAmountField);
        root.add(Box.createVerticalStrut(14));

        // Month + Year row
        JPanel dateRow = new JPanel(new GridLayout(1, 2, 12, 0));
        dateRow.setOpaque(false);
        dateRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));

        JPanel monthCol = new JPanel();
        monthCol.setLayout(new BoxLayout(monthCol, BoxLayout.Y_AXIS));
        monthCol.setOpaque(false);
        monthCol.add(formLabel("Month"));
        monthCol.add(Box.createVerticalStrut(4));
        String[] monthNames = {"January","February","March","April","May","June",
                               "July","August","September","October","November","December"};
        dlgMonthBox = new JComboBox<>(monthNames);
        dlgMonthBox.setSelectedIndex(selectedMonth - 1);
        dlgMonthBox.setFont(sf(Font.PLAIN, 13f));
        dlgMonthBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        monthCol.add(dlgMonthBox);
        dateRow.add(monthCol);

        JPanel yearCol = new JPanel();
        yearCol.setLayout(new BoxLayout(yearCol, BoxLayout.Y_AXIS));
        yearCol.setOpaque(false);
        yearCol.add(formLabel("Year"));
        yearCol.add(Box.createVerticalStrut(4));
        dlgYearSpinner = new JSpinner(new SpinnerNumberModel(selectedYear, 2000, 2099, 1));
        dlgYearSpinner.setFont(sf(Font.PLAIN, 13f));
        dlgYearSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        yearCol.add(dlgYearSpinner);
        dateRow.add(yearCol);

        root.add(dateRow);
        root.add(Box.createVerticalStrut(24));

        // Buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JButton cancel = buildGrayButton("Cancel");
        cancel.addActionListener(e -> dlg.dispose());

        JButton add = buildBlueButton("Add");
        add.addActionListener(e -> {
            try {
                String cat    = (String) dlgCategoryBox.getSelectedItem();
                String amtTxt = dlgAmountField.getText().trim();
                if (amtTxt.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Please enter an amount.", "Required", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                double amount = Double.parseDouble(amtTxt);
                int    month  = dlgMonthBox.getSelectedIndex() + 1;
                int    year   = (int) dlgYearSpinner.getValue();
                compute.addExpense(year, month, cat, amount);
                dlg.dispose();
                updateCharts();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Enter a valid number.", "Invalid", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(cancel);
        btnRow.add(add);
        root.add(btnRow);

        dlg.setContentPane(root);

        // Press Enter to submit
        dlg.getRootPane().setDefaultButton(add);
        dlg.setVisible(true);
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

        // Page title
        /*JLabel pageTitle = new JLabel("Overview");
        pageTitle.setFont(sf(Font.BOLD, 20f));
        pageTitle.setForeground(LABEL);
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(pageTitle);*/

        /*JLabel pageSub = new JLabel("Your spending at a glance");
        pageSub.setFont(sf(Font.PLAIN, 13f));
        pageSub.setForeground(LABEL_2);
        pageSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(pageSub);
        inner.add(Box.createVerticalStrut(20));*/

        // ── Row 1: 4 stat cards ───────────────────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        statsRow.add(buildStatCard("Yearly Total",  yearlyTotalLabelRef(),          ACCENT,  "◈"));
        statsRow.add(buildStatCard("vs Last Month", monthChangeLabelRef(),           GREEN,  "↑"));
        statsRow.add(buildStatCard("Avg / Month",   averageExpensesLabelRef(),       INDIGO, "⌀"));
        statsRow.add(buildStatCard("This Month",    currentMonthSpendingLabelRef(),  ORANGE, "●"));
        inner.add(statsRow);
        inner.add(Box.createVerticalStrut(14));

        // ── Row 2: 2 charts ───────────────────────────────────────────────────
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

        // ── Row 3: category breakdown ─────────────────────────────────────────
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

        // Tinted icon circle
        JPanel iconCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color c = (monthChangeIconCircle == this && monthChangeAccent != null)
                        ? monthChangeAccent
                        : accent;

                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.dispose();
            }

            @Override public Dimension getPreferredSize() {
                return new Dimension(34, 34);
            }
        };

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(sf(Font.PLAIN, 15f));
        iconLabel.setForeground(accent);

        iconCircle.add(iconLabel);

        iconCircle.setOpaque(false);
        iconCircle.setLayout(new GridBagLayout());

        if (title.equals("vs Last Month")) {
        monthChangeIconLabel = iconLabel;
        monthChangeIconCircle = iconCircle;
        monthChangeAccent = accent; // initial (green)
}

        JPanel iconWrap = new JPanel(new GridBagLayout());
        iconWrap.setOpaque(false);
        iconWrap.add(iconCircle);

        // Text block
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

    private void updateCategoryBreakdown() {
        if (categorySummaryPanel == null) return;
        categorySummaryPanel.removeAll();
        Map<String, Double> totals = compute.getTotalsByCategory(selectedYear, null);
        if (totals.isEmpty()) {
            JLabel e = new JLabel("No data for " + selectedYear);
            e.setFont(sf(Font.PLAIN, 13f));
            e.setForeground(LABEL_3);
            categorySummaryPanel.add(e);
        } else {
            int ci = 0;
            for (Map.Entry<String, Double> entry : totals.entrySet()) {
                categorySummaryPanel.add(buildCategoryChip(entry.getKey(), entry.getValue(), CAT_COLORS[ci % CAT_COLORS.length]));
                ci++;
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
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // dot
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
        amtLbl.setFont(UIManager.getFont(	"h3.font"));
        amtLbl.setForeground(LABEL);

        textCol.add(catLbl);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(amtLbl);

        chip.add(dot,    BorderLayout.WEST);
        chip.add(textCol, BorderLayout.CENTER);
        return chip;
    }

    // ════════════════════════════════════════════════════════════════════════
    // UPDATE CHARTS
    // ════════════════════════════════════════════════════════════════════════
    private void updateCharts() {
        chartLeftPanel.removeAll();
        chartRightPanel.removeAll();
        try {
            double monthlyTotal = compute.getMonthlyTotal(selectedYear, selectedMonth);
            double avgMonthly   = compute.getAverageMonthlyExpenses(selectedYear);
            double yearlyTotal  = compute.getYearlyTotal(selectedYear);
            double monthChange  = compute.getMonthComparison(selectedYear, selectedMonth-1, selectedMonth);

            currentMonthSpendingLabel.setText(String.format("RM %.2f", monthlyTotal));
            averageExpensesLabel.setText(String.format("RM %.2f", avgMonthly));
            yearlyTotalLabel.setText(String.format("RM %.2f", yearlyTotal));
            monthChangeLabel.setText(monthChange != 0 
                    ? String.format("%+.1f%%", monthChange) 
                    : "—");

            if (monthChange < 0) {
                monthChangeLabel.setForeground(RED);

                monthChangeIconLabel.setText("↓");
                monthChangeIconLabel.setForeground(RED);

                monthChangeAccent = RED;

            } else if (monthChange > 0) {
                monthChangeLabel.setForeground(GREEN);

                monthChangeIconLabel.setText("↑");
                monthChangeIconLabel.setForeground(GREEN);

                monthChangeAccent = GREEN;

            } else {
                monthChangeLabel.setForeground(LABEL_2);

                monthChangeIconLabel.setText("—");
                monthChangeIconLabel.setForeground(LABEL_2);

                monthChangeAccent = LABEL_2;
            }

            // 🔥 IMPORTANT: repaint the circle
            monthChangeIconCircle.repaint();

            if (currentViewMode == 1) {
                Map<Integer, Double> trend   = compute.getMonthlyTotals(selectedYear);
                Map<String, Double>  monthly = compute.getTotalsByCategory(selectedYear, selectedMonth);
                ChartFrame lf = new ChartFrame(theme(ChartLine.createMonthlyTrendChart(trend, selectedYear)));
                lf.setPreferredSize(new Dimension(400, 320));
                ChartFrame pf = new ChartFrame(theme(ChartPie.createMonthlyPieChart(monthly, selectedYear, selectedMonth)));
                pf.setPreferredSize(new Dimension(340, 320));
                chartLeftPanel.add(pf, BorderLayout.CENTER);
                chartRightPanel.add(lf, BorderLayout.CENTER);
            } else {
                Map<String, Double> yearly = compute.getTotalsByCategory(selectedYear, null);
                ChartFrame bf = new ChartFrame(theme(ChartBar.createCategoryBarChart(yearly, "Yearly Overview", "Category", "Amount (RM)")));
                bf.setPreferredSize(new Dimension(400, 320));
                ChartFrame pf = new ChartFrame(theme(ChartPie.createYearlyPieChart(yearly, selectedYear)));
                pf.setPreferredSize(new Dimension(340, 320));
                chartLeftPanel.add(pf, BorderLayout.CENTER);
                chartRightPanel.add(bf, BorderLayout.CENTER);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JLabel err = new JLabel("Database connection required.");
            err.setForeground(LABEL_3);
            err.setFont(sf(Font.PLAIN, 12f));
            err.setHorizontalAlignment(JLabel.CENTER);
            chartLeftPanel.add(err, BorderLayout.CENTER);
        }
        chartLeftPanel.revalidate();  chartLeftPanel.repaint();
        chartRightPanel.revalidate(); chartRightPanel.repaint();
        updateCategoryBreakdown();
    }

    private JFreeChart theme(JFreeChart chart) {
        chart.setBackgroundPaint(CARD_BG);
        chart.getPlot().setBackgroundPaint(CARD_BG);
        if (chart.getTitle() != null) {
            chart.getTitle().setPaint(LABEL);
            chart.getTitle().setFont(UIManager.getFont(	"h3.font"));
        }
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(CARD_BG);
            chart.getLegend().setItemPaint(LABEL_2);
            chart.getLegend().setItemFont(sf(Font.PLAIN, 11f));
        }
        if (chart.getPlot() instanceof org.jfree.chart.plot.CategoryPlot) {
            org.jfree.chart.plot.CategoryPlot p = (org.jfree.chart.plot.CategoryPlot) chart.getPlot();
            p.setRangeGridlinePaint(SEP_LIGHT);
            p.getDomainAxis().setTickLabelPaint(LABEL_2);
            p.getDomainAxis().setLabelPaint(LABEL_2);
            p.getDomainAxis().setAxisLinePaint(SEP_LIGHT);
            p.getDomainAxis().setTickLabelFont(sf(Font.PLAIN, 10f));
            p.getRangeAxis().setTickLabelPaint(LABEL_2);
            p.getRangeAxis().setLabelPaint(LABEL_2);
            p.getRangeAxis().setAxisLinePaint(SEP_LIGHT);
            p.getRangeAxis().setTickLabelFont(sf(Font.PLAIN, 10f));
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
                // subtle diffuse shadow
                for (int i = 5; i >= 1; i--) {
                    g2.setColor(new Color(0,0,0, 5));
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
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        return card;
    }

    private JLabel yearlyTotalLabelRef() {
        yearlyTotalLabel = new JLabel("RM 0.00"); return yearlyTotalLabel;
    }
    private JLabel monthChangeLabelRef() {
        monthChangeLabel = new JLabel("—"); return monthChangeLabel;
    }
    private JLabel averageExpensesLabelRef() {
        averageExpensesLabel = new JLabel("RM 0.00"); return averageExpensesLabel;
    }
    private JLabel currentMonthSpendingLabelRef() {
        currentMonthSpendingLabel = new JLabel("RM 0.00"); return currentMonthSpendingLabel;
    }

    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(sf(Font.PLAIN, 12f));
        l.setForeground(LABEL_2);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel smallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(sf(Font.PLAIN, 12f));
        l.setForeground(LABEL_2);
        return l;
    }

    private Component thinRule(int w, int h) {
        JPanel r = new JPanel();
        r.setBackground(SEP);
        r.setPreferredSize(new Dimension(w, h));
        r.setMaximumSize(new Dimension(w, h));
        return r;
    }

    private JButton buildBlueButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(0x0060CC) : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setFont(getFont()); g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(sf(Font.PLAIN, 13f));
        btn.setPreferredSize(new Dimension(80, 30));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton buildGrayButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? new Color(0xD0D0D5) : new Color(0xE5E5EA));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setFont(getFont()); g2.setColor(LABEL);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2,
                    (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        btn.setFont(sf(Font.PLAIN, 13f));
        btn.setPreferredSize(new Dimension(80, 30));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleScrollBar(JScrollBar bar) {
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0xC6C6C8); trackColor = BG;
            }
            @Override protected JButton createDecreaseButton(int o) { return zero(); }
            @Override protected JButton createIncreaseButton(int o) { return zero(); }
            JButton zero() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // MacBorder — hairline rounded border
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