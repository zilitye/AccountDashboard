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

/**
 * Main application window for the Expenses Dashboard.
 *
 * Extends JFrame to act as the top-level window. It is structured around
 * three "pages" (Overview, Database, Settings) that are swapped in and out
 * using a CardLayout. The Overview page contains:
 *   - A title/toolbar bar at the top (month/year pickers, Add Expense button)
 *   - A collapsible sidebar on the left for navigation
 *   - A main canvas with stat cards, charts, and a category breakdown
 *
 * All database work is done off the EDT (Event Dispatch Thread) using
 * SwingWorker so the UI never freezes.
 */
public class ExpensesComputeApplication extends JFrame {

    // ── UI panel references ──────────────────────────────────────────────────
    // Held as fields so updateCharts() can swap their contents later.
    private JPanel   chartLeftPanel, chartRightPanel;   // chart card containers
    private JPanel   categorySummaryPanel;              // grid of category chips

    // Stat-card labels — updated every time the DB data reloads
    private JLabel   currentMonthSpendingLabel,  // "This Month" value
                     averageExpensesLabel,        // "Avg / Month" value
                     yearlyTotalLabel,            // "Yearly Total" value
                     monthChangeLabel;            // "vs Last Month" percentage

    // Segmented-control buttons ("Monthly" / "Yearly") in the toolbar
    private JButton  tabButtonMonth, tabButtonYear;

    // Dropdown for selecting the month (January–December) in the toolbar
    private JComboBox<String> monthSelector;

    // 1 = Monthly view, 2 = Yearly view — drives which charts are rendered
    private int      currentViewMode = 1;

    // ── "vs Last Month" card accent state ────────────────────────────────────
    // These three references let updateCharts() recolor the icon + circle
    // depending on whether spending went up (red) or down (green).
    private JLabel monthChangeIconLabel;
    private JPanel monthChangeIconCircle;
    private Color  monthChangeAccent;

    // ── Add-expense dialog fields ─────────────────────────────────────────────
    // Created fresh each time showAddExpenseDialog() is called; held here so
    // the "Add" button's ActionListener can read them without inner-class tricks.
    private JComboBox<String> dlgCategoryBox;
    private JTextField        dlgAmountField;
    private JComboBox<String> dlgMonthBox;
    private JSpinner          dlgYearSpinner;

    // ── Background-loading state ──────────────────────────────────────────────
    // Keeps a reference to the most recently submitted SwingWorker.
    // When the user changes month/year quickly, the old worker is cancelled
    // so its stale done() callback never overwrites fresher data.
    private SwingWorker<Void,Void> pendingWorker;

    // Business-logic layer — wraps all SQL queries
    private ExpensesCompute compute;

    // Currently selected year/month — default to today's date
    private int selectedYear  = LocalDate.now().getYear();
    private int selectedMonth = LocalDate.now().getMonthValue();

    // ── Page routing (CardLayout) ─────────────────────────────────────────────
    private JPanel        pageStack;      // the CardLayout host panel
    private CardLayout    cardLayout;
    private DatabasePage  databasePage;   // shows raw DB records
    private SettingsPage  settingsPage;   // app settings
    private JPanel        overviewPage;   // the main dashboard canvas
    private String        activePage = "Overview";

    // Sidebar nav row panel references — needed to toggle the blue highlight
    private JPanel navOverview, navDatabase, navSettings;

    // ── macOS Sonoma design tokens ────────────────────────────────────────────
    // Colors are matched to Apple's semantic system palette so the app looks
    // native on macOS without requiring native APIs.
    private static final Color BG           = new Color(0xf6f6f6);  // window background
    private static final Color SIDEBAR_BG   = new Color(0xf6f6f6);  // sidebar tint
    private static final Color CARD_BG      = Color.WHITE;           // card surfaces
    private static final Color TOOLBAR_BG   = new Color(0xf6f6f6);  // title bar

    private static final Color SEP          = new Color(0xC6C6C8);  // full separator
    private static final Color SEP_LIGHT    = new Color(0xE5E5EA);  // subtle separator

    private static final Color LABEL        = new Color(0x1C1C1E);  // primary text
    private static final Color LABEL_2      = new Color(0x6E6E73);  // secondary text
    private static final Color LABEL_3      = new Color(0xAEAEB2);  // tertiary / placeholder

    private static final Color ACCENT       = new Color(0x007AFF);  // iOS blue
    private static final Color INDIGO       = new Color(0x5856D6);
    private static final Color GREEN        = new Color(0x34C759);
    private static final Color RED          = new Color(0xFF3B30);
    private static final Color ORANGE       = new Color(0xFF9500);

    // One color per expense category, cycling if there are more categories than colors
    private static final Color[] CAT_COLORS = {
        ACCENT, GREEN, ORANGE, RED, INDIGO,
        new Color(0xFF2D55), new Color(0x30B0C7)
    };

    private static final int R          = 10;   // corner radius used on all cards
    private static final int SIDEBAR_W  = 220;  // fixed width of the left sidebar

    // ── Font helper ───────────────────────────────────────────────────────────
    /**
     * Returns the best San Francisco / Helvetica Neue font available on the
     * current platform, falling back to "SansSerif" on non-Apple systems.
     *
     * @param style Font.PLAIN / Font.BOLD / Font.ITALIC
     * @param size  point size (float for sub-pixel accuracy)
     */
    private static Font sf(int style, float size) {
        for (String n : new String[]{".SF NS Display",".SF NS Text","Helvetica Neue","Helvetica","SansSerif"}) {
            Font f = new Font(n, style, (int) size);
            if (!f.getFamily().equals("Dialog")) return f.deriveFont(size);
        }
        return new Font("SansSerif", style, (int) size);
    }

    // ════════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // Sets up the look-and-feel, builds every UI region, packs it all into
    // the JFrame, then kicks off the first data load on the EDT.
    // ════════════════════════════════════════════════════════════════════════
    public ExpensesComputeApplication() {
        super("Account Dashboard");
        compute = new ExpensesCompute(); // initialise the DB/business logic layer

        // Apply FlatLaf's macOS Light theme and override a few defaults
        // so rounded corners and focus rings match the custom design.
        try {
            UIManager.setLookAndFeel(new FlatMacLightLaf());
            UIManager.put("Component.focusWidth",  0);   // no focus ring glow
            UIManager.put("Button.arc",            R);
            UIManager.put("TextComponent.arc",     R);
            UIManager.put("ComboBox.arc",          R);
            UIManager.put("Panel.background",      BG);
            UIManager.put("ScrollBar.width",       7);   // slim scrollbar
            UIManager.put("ScrollBar.thumbArc",    999); // fully rounded thumb
        } catch (Exception e) { e.printStackTrace(); }

        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        // Title bar spans the full top edge (above sidebar + main content)
        add(buildTitleBar(), BorderLayout.NORTH);

        // Instantiate each page — only one is visible at a time via CardLayout
        overviewPage = buildMainCanvas();
        databasePage = new DatabasePage();
        settingsPage = new SettingsPage();

        // Register pages with the card layout using their display names as keys
        cardLayout = new CardLayout();
        pageStack  = new JPanel(cardLayout);
        pageStack.setBackground(BG);
        pageStack.add(overviewPage, "Overview");
        pageStack.add(databasePage, "Database");
        pageStack.add(settingsPage, "Settings");

        // body = sidebar (left) + page stack (center)
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(BG);
        body.add(buildSidebar(),  BorderLayout.WEST);
        body.add(pageStack,       BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        setSize(1340, 840);
        setMinimumSize(new Dimension(980, 640));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // centre on screen
        setVisible(true);

        // Defer the initial paint + data load until the window is fully shown
        // so the layout pass has already run and panel sizes are known.
        SwingUtilities.invokeLater(() -> {
            refreshNavHighlights("Overview");
            updateCharts();
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // TITLE BAR
    // A three-column panel (left / center / right) painted manually so it
    // matches the macOS toolbar colour exactly.
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildTitleBar() {
        // Custom paintComponent draws the toolbar background + bottom hairline
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(TOOLBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // 1-px bottom border to visually separate bar from content
                g2.setColor(SEP);
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 50));

        // ── LEFT: app title, width matches the sidebar so text stays aligned ──
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

        // ── CENTER: Monthly/Yearly toggle + month/year pickers ────────────────
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 9));
        center.setOpaque(false);

        center.add(buildSegmentedControl()); // "Monthly" | "Yearly" pill buttons
        center.add(thinRule(1, 24));         // vertical divider

        center.add(smallLabel("Month"));
        String[] months = {"January","February","March","April","May","June",
                           "July","August","September","October","November","December"};
        monthSelector = new JComboBox<>(months);
        monthSelector.setSelectedIndex(selectedMonth - 1);
        monthSelector.setFont(sf(Font.PLAIN, 12f));
        monthSelector.setPreferredSize(new Dimension(118, 28));
        // Changing the month immediately triggers a chart refresh
        monthSelector.addActionListener(e -> { selectedMonth = monthSelector.getSelectedIndex()+1; updateCharts(); });
        center.add(monthSelector);

        center.add(thinRule(1, 24));
        center.add(smallLabel("Year"));

        // Year spinner: 2000–2099, step 1
        SpinnerNumberModel ym = new SpinnerNumberModel(selectedYear, 2000, 2099, 1);
        JSpinner yearSpinner = new JSpinner(ym);
        yearSpinner.setFont(sf(Font.PLAIN, 12f));
        yearSpinner.setPreferredSize(new Dimension(72, 28));
        yearSpinner.addChangeListener(e -> { selectedYear = (int) yearSpinner.getValue(); updateCharts(); });
        center.add(yearSpinner);

        bar.add(center, BorderLayout.CENTER);

        // ── RIGHT: primary action button ──────────────────────────────────────
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 9));
        right.setOpaque(false);
        right.add(buildToolbarAddButton());
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    /**
     * Builds the blue "+ Add Expense" button in the toolbar.
     * Painted manually (no L&F border) so it stays blue on all platforms.
     */
    private JButton buildToolbarAddButton() {
        JButton btn = new JButton("+ Add Expense") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Darken slightly when pressed for tactile feedback
                g2.setColor(getModel().isPressed() ? new Color(0x0065D9) : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setFont(getFont());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                // Centre the label text manually
                g2.drawString(getText(),
                    (getWidth() - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(sf(Font.PLAIN, 13f));
        btn.setPreferredSize(new Dimension(130, 30));
        btn.setContentAreaFilled(false); // let paintComponent handle the fill
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> showAddExpenseDialog());
        return btn;
    }

    // ════════════════════════════════════════════════════════════════════════
    // SEGMENTED CONTROL  ("Monthly" / "Yearly" pill)
    // ════════════════════════════════════════════════════════════════════════

    /** Wraps both segment buttons inside a rounded grey container. */
    private JPanel buildSegmentedControl() {
        JPanel seg = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xDEDEE3)); // pill track background
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
                g2.dispose();
            }
        };
        seg.setOpaque(false);
        seg.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3)); // padding inside track

        tabButtonMonth = buildSegBtn("Monthly", true);   // selected by default
        tabButtonYear  = buildSegBtn("Yearly",  false);
        seg.add(tabButtonMonth);
        seg.add(tabButtonYear);
        return seg;
    }

    /**
     * Creates a single segment button (one of the two pills).
     * When active it renders a white rounded "thumb" with a drop shadow;
     * when inactive it is transparent so the grey track shows through.
     *
     * @param text   Button label
     * @param active Whether this button starts in the selected state
     */
    private JButton buildSegBtn(String text, boolean active) {
        JButton btn = new JButton(text) {
            // Convenience: a button is active when its background == CARD_BG (white)
            boolean isActive() { return getBackground() == CARD_BG; }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isActive()) {
                    // Shadow pass (offset 1px down)
                    g2.setColor(new Color(0, 0, 0, 25));
                    g2.fillRoundRect(1, 2, getWidth(), getHeight(), 7, 7);
                    // White thumb
                    g2.setColor(CARD_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 7, 7);
                }
                // Draw label centred
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
            activateSeg(btn);                                  // update visual state
            currentViewMode = text.equals("Monthly") ? 1 : 2; // 1 = monthly, 2 = yearly
            updateCharts();
        });
        return btn;
    }

    /**
     * Swaps the active state to the clicked segment button and resets the other.
     * Operates directly on the two button references rather than a loop so that
     * adding more segments in the future requires explicit changes here.
     */
    private void activateSeg(JButton active) {
        for (JButton b : new JButton[]{tabButtonMonth, tabButtonYear}) {
            if (b == null) continue;
            b.setBackground(new Color(0,0,0,0)); // transparent = inactive
            b.setForeground(LABEL_2);
            b.setFont(sf(Font.PLAIN, 12.5f));
        }
        active.setBackground(CARD_BG); // white = active
        active.setForeground(LABEL);
        active.setFont(sf(Font.BOLD, 12.5f));
        tabButtonMonth.repaint();
        tabButtonYear.repaint();
    }

    // ════════════════════════════════════════════════════════════════════════
    // SIDEBAR
    // A fixed-width panel on the left with a hairline right border and
    // nav items for Overview, Database, and Settings.
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(SIDEBAR_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // 1-px right border separates sidebar from main content
                g2.setColor(SEP);
                g2.fillRect(getWidth()-1, 0, 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_W, 0));

        // Navigation item list (vertical BoxLayout)
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        nav.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        nav.add(sidebarSectionLabel("MENU")); // all-caps section header
        nav.add(Box.createVerticalStrut(4));

        // icon, page name — index 0 starts as active
        String[][] items = {
            {"⊞", "Overview"},
            {"◫", "Database"},
            {"⚙", "Settings"},
        };
        for (int i = 0; i < items.length; i++) {
            final String pageName = items[i][1];
            boolean isActive = (i == 0); // Overview is selected on launch
            JPanel navItem = buildNavItem(items[i][0], pageName, isActive, () -> switchPage(pageName));
            // Save references so refreshNavHighlights() can find them later
            if (pageName.equals("Overview"))  navOverview  = navItem;
            if (pageName.equals("Database"))  navDatabase  = navItem;
            if (pageName.equals("Settings"))  navSettings  = navItem;
            nav.add(navItem);
        }

        sidebar.add(nav, BorderLayout.NORTH);
        return sidebar;
    }

    /** Creates the small all-caps "MENU" section header above the nav items. */
    private JPanel sidebarSectionLabel(String text) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel lbl = new JLabel(text);
        lbl.setFont(sf(Font.BOLD, 10f));
        lbl.setForeground(LABEL_3);

        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        wrapper.add(lbl, BorderLayout.WEST);
        return wrapper;
    }

    /**
     * Builds one nav row: an icon + label inside a hoverable/clickable panel.
     *
     * The active state is stored as a client property ("navActive") so
     * refreshNavHighlights() can toggle it without rebuilding the row.
     * Hover state ("hover") is toggled by MouseListener enter/exit events.
     * Both states are read in paintComponent to draw the correct background.
     *
     * @param icon    Unicode symbol shown on the left
     * @param label   Page name shown as text
     * @param active  Whether this row starts highlighted
     * @param onClick Callback invoked when the row is clicked
     */
    private JPanel buildNavItem(String icon, String label, boolean active, Runnable onClick) {

        JPanel row = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                boolean isActive = Boolean.TRUE.equals(getClientProperty("navActive"));
                boolean hover    = Boolean.TRUE.equals(getClientProperty("hover"));

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isActive) {
                    // Solid blue pill for the selected page
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(6, 3, getWidth()-12, getHeight()-6, 8, 8);
                } else if (hover) {
                    // Subtle dark tint on hover (not selected)
                    g2.setColor(new Color(0, 0, 0, 18));
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

        // Inner panel holds icon + label with horizontal spacing
        JPanel inner = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        inner.setOpaque(false);

        JLabel ico = new JLabel(icon);
        ico.setFont(sf(Font.PLAIN, 14f));
        ico.setPreferredSize(new Dimension(18, 18));
        ico.setForeground(active ? Color.WHITE : LABEL_2);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIManager.getFont(active ? "h3.font" : "h3.regular.font"));
        lbl.setForeground(active ? Color.WHITE : LABEL);

        inner.add(ico);
        inner.add(lbl);
        row.add(inner, BorderLayout.CENTER);

        // Hover feedback + click routing
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                row.putClientProperty("hover", true);
                row.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                row.putClientProperty("hover", false);
                row.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (onClick != null) onClick.run();
            }
        });

        return row;
    }

    // ════════════════════════════════════════════════════════════════════════
    // PAGE SWITCHER
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Shows the named page in the CardLayout and refreshes its data.
     * No-ops if the requested page is already active to avoid unnecessary reloads.
     */
    private void switchPage(String pageName) {
        if (pageName.equals(activePage)) return;
        activePage = pageName;

        cardLayout.show(pageStack, pageName);

        // Each page re-fetches its data on every visit so it never shows stale results
        if (pageName.equals("Database") && databasePage != null) {
            databasePage.loadData();
        }
        if (pageName.equals("Overview")) {
            updateCharts();
        }

        refreshNavHighlights(pageName);
    }

    /**
     * Walks the three nav row panels and updates colours/font/client-property
     * so the blue pill tracks the active page.
     *
     * This avoids rebuilding the sidebar on every navigation click — only the
     * foreground colours and the "navActive" flag need to change.
     */
    private void refreshNavHighlights(String activeName) {
        JPanel[] rows  = {navOverview, navDatabase, navSettings};
        String[] names = {"Overview",  "Database",  "Settings"};

        for (int i = 0; i < rows.length; i++) {
            if (rows[i] == null) continue;
            final boolean active = names[i].equals(activeName);

            // Walk the component tree: row → inner JPanel → JLabel (icon or text)
            for (Component child : rows[i].getComponents()) {
                if (child instanceof JPanel) {
                    JPanel inner = (JPanel) child;
                    for (Component c2 : inner.getComponents()) {
                        if (c2 instanceof JLabel) {
                            JLabel lbl = (JLabel) c2;
                            // Icons are 1–2 chars; labels are longer — different inactive colours
                            boolean isIcon = lbl.getText().length() <= 2;
                            lbl.setForeground(active ? Color.WHITE : (isIcon ? LABEL_2 : LABEL));
                            lbl.setFont(UIManager.getFont(active ? "h3.font" : "h3.regular.font"));
                        }
                    }
                }
            }
            rows[i].putClientProperty("navActive", active); // triggers blue pill in paintComponent
            rows[i].repaint();
        }
        setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    // ADD EXPENSE DIALOG
    // A modal JDialog with fields for: category, amount, month, and year.
    // On "Add", the expense is written to the DB and the dashboard reloads.
    // ════════════════════════════════════════════════════════════════════════
    private void showAddExpenseDialog() {
        JDialog dlg = new JDialog(this, "Add Expense", true); // modal = blocks parent
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

        // ── Category dropdown ─────────────────────────────────────────────────
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

        // ── Amount text field ─────────────────────────────────────────────────
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

        // ── Month + Year in a 2-column grid ───────────────────────────────────
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
        dlgMonthBox.setSelectedIndex(selectedMonth - 1); // pre-select toolbar month
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

        // ── Cancel / Add buttons ──────────────────────────────────────────────
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
                int    month  = dlgMonthBox.getSelectedIndex() + 1; // 1-based month
                int    year   = (int) dlgYearSpinner.getValue();
                compute.addExpense(year, month, cat, amount); // persist to DB
                dlg.dispose();
                updateCharts(); // reflect the new expense in the dashboard
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Enter a valid number.", "Invalid", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(cancel);
        btnRow.add(add);
        root.add(btnRow);

        dlg.setContentPane(root);
        dlg.getRootPane().setDefaultButton(add); // press Enter to submit
        dlg.setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    // MAIN CANVAS  (the "Overview" page)
    // Three stacked rows inside a scroll pane:
    //   Row 1 — 4 stat cards
    //   Row 2 — 2 chart panels (pie/bar/line depending on view mode)
    //   Row 3 — category breakdown chips
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildMainCanvas() {
        JPanel canvas = new JPanel(new BorderLayout());
        canvas.setBackground(BG);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(BG);
        inner.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        // ── Row 1: 4 stat cards ───────────────────────────────────────────────
        // Each card shows a coloured icon circle, a title, and a value label.
        // The *LabelRef() helpers create the JLabel AND store the field reference.
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        statsRow.add(buildStatCard("Yearly Total",  yearlyTotalLabelRef(),          ACCENT,  "◈"));
        statsRow.add(buildStatCard("vs Last Month", monthChangeLabelRef(),           RED,    "↑"));
        statsRow.add(buildStatCard("Avg / Month",   averageExpensesLabelRef(),       INDIGO, "⌀"));
        statsRow.add(buildStatCard("This Month",    currentMonthSpendingLabelRef(),  ORANGE, "●"));
        inner.add(statsRow);
        inner.add(Box.createVerticalStrut(14));

        // ── Row 2: 2 chart containers ─────────────────────────────────────────
        // The panels are empty shells here; updateCharts() injects ChartFrame
        // children once the DB data is available.
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

        // ── Row 3: category chips ─────────────────────────────────────────────
        inner.add(buildCategoryBreakdownCard());
        inner.add(Box.createVerticalGlue()); // push everything to the top

        // Wrap in a scroll pane so the layout survives very small windows
        JScrollPane sp = new JScrollPane(inner);
        sp.setBorder(null);
        sp.setBackground(BG);
        sp.getViewport().setBackground(BG);
        sp.getVerticalScrollBar().setUnitIncrement(16); // smooth mouse-wheel scrolling
        styleScrollBar(sp.getVerticalScrollBar());
        canvas.add(sp, BorderLayout.CENTER);
        return canvas;
    }

    // ════════════════════════════════════════════════════════════════════════
    // STAT CARD
    // A white card with a tinted icon circle on the left and a title +
    // value label on the right.  The "vs Last Month" card stores extra
    // references so updateCharts() can change its colour dynamically.
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildStatCard(String title, JLabel valueLabel, Color accent, String icon) {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(14, 0));

        // The icon circle repaints itself using monthChangeAccent when it
        // belongs to the "vs Last Month" card, so a single repaint() call
        // from updateCharts() is all it takes to switch colours.
        JPanel iconCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Use the dynamic accent for the "vs Last Month" card, static for others
                Color c = (monthChangeIconCircle == this && monthChangeAccent != null)
                        ? monthChangeAccent
                        : accent;
                g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 30)); // 12% opacity fill
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(34, 34); }
        };

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(sf(Font.PLAIN, 15f));
        iconLabel.setForeground(accent);

        iconCircle.add(iconLabel);
        iconCircle.setOpaque(false);
        iconCircle.setLayout(new GridBagLayout()); // centres the icon inside the circle

        // Save references for the "vs Last Month" card so we can recolour it
        if (title.equals("vs Last Month")) {
            monthChangeIconLabel  = iconLabel;
            monthChangeIconCircle = iconCircle;
            monthChangeAccent     = accent; // initial colour (RED)
        }

        JPanel iconWrap = new JPanel(new GridBagLayout()); // vertically centres the circle
        iconWrap.setOpaque(false);
        iconWrap.add(iconCircle);

        // Text column: small title above, bold value below
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
    // CATEGORY BREAKDOWN CARD
    // A card with a "Yearly Breakdown" header above a grid of category chips.
    // The grid is stored in categorySummaryPanel and rebuilt by
    // updateCategoryBreakdown() on every data load.
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildCategoryBreakdownCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel title = new JLabel("Yearly Breakdown");
        title.setFont(UIManager.getFont("h3.font"));
        title.setForeground(LABEL);
        card.add(title, BorderLayout.NORTH);

        // GridLayout auto-wraps chips into rows of 4
        categorySummaryPanel = new JPanel(new GridLayout(0, 4, 10, 8));
        categorySummaryPanel.setBackground(CARD_BG);
        card.add(categorySummaryPanel, BorderLayout.CENTER);
        return card;
    }

    /**
     * Rebuilds the category chip grid with fresh data.
     * Called from the SwingWorker's done() — always runs on the EDT.
     *
     * @param totals Map of category name → yearly total, or null if no DB connection
     */
    private void updateCategoryBreakdown(Map<String, Double> totals) {
        if (categorySummaryPanel == null) return;
        categorySummaryPanel.removeAll(); // clear previous chips

        if (totals == null || totals.isEmpty()) {
            // Show a single placeholder message instead of chips
            JLabel e = new JLabel(totals == null ? "No database connection." : "No data for " + selectedYear);
            e.setFont(sf(Font.PLAIN, 13f));
            e.setForeground(LABEL_3);
            categorySummaryPanel.add(e);
        } else {
            int ci = 0;
            for (Map.Entry<String, Double> entry : totals.entrySet()) {
                categorySummaryPanel.add(
                    buildCategoryChip(entry.getKey(), entry.getValue(), CAT_COLORS[ci % CAT_COLORS.length])
                );
                ci++;
            }
        }
        categorySummaryPanel.revalidate();
        categorySummaryPanel.repaint();
    }

    /**
     * Builds one category chip: a coloured-border card with a dot indicator,
     * the category name, and its total amount.
     */
    private JPanel buildCategoryChip(String category, double amount, Color accent) {
        JPanel chip = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Very light tinted fill using the category accent colour
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

        // Small 8×8 filled circle used as a colour key
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, (getHeight()-8)/2, 8, 8); // vertically centred
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
    // The central refresh method. Every time the user changes month, year,
    // or view mode, a new SwingWorker is spawned. Any previously running
    // worker is cancelled so its stale done() result is silently discarded.
    //
    // doInBackground()  — runs on a worker thread: DB queries + chart builds
    // done()            — runs on the EDT: swaps UI components atomically
    // ════════════════════════════════════════════════════════════════════════
    public void updateCharts() {
        // Cancel any in-flight worker so its done() is a no-op
        if (pendingWorker != null && !pendingWorker.isDone()) {
            pendingWorker.cancel(false);
        }

        // Capture current selections — the worker closes over these snapshots
        // so a mid-flight UI change doesn't corrupt the worker's data.
        final int year  = selectedYear;
        final int month = selectedMonth;
        final int mode  = currentViewMode;

        pendingWorker = new SwingWorker<Void, Void>() {

            // Results computed off the EDT and read in done()
            private boolean      dbOk = false;
            private double       monthlyTotal, avgMonthly, yearlyTotal, monthChange;
            private JFreeChart   leftChart, rightChart;
            private Map<String, Double> catTotals;

            @Override
            protected Void doInBackground() {
                try {
                    Connection conn = SQLConnection.getInstance().getConnection();
                    if (conn == null || conn.isClosed()) return null; // bail → dbOk stays false

                    // Scalar KPI metrics for the 4 stat cards
                    monthlyTotal = compute.getMonthlyTotal(year, month);
                    avgMonthly   = compute.getAverageMonthlyExpenses(year);
                    yearlyTotal  = compute.getYearlyTotal(year);
                    monthChange  = compute.getMonthComparison(year, month - 1, month); // % change

                    // Category totals for the bottom breakdown section
                    catTotals = compute.getTotalsByCategory(year, null);

                    if (mode == 1) {
                        // Monthly mode: pie (this month's categories) + line trend (all months)
                        Map<Integer, Double> trend   = compute.getMonthlyTotals(year);
                        Map<String, Double>  monthly = compute.getTotalsByCategory(year, month);
                        leftChart  = theme(ChartPie.createMonthlyPieChart(monthly, year, month));
                        rightChart = theme(ChartLine.createMonthlyTrendChart(trend, year));
                    } else {
                        // Yearly mode: pie (all categories) + bar (category comparison)
                        Map<String, Double> yearly = compute.getTotalsByCategory(year, null);
                        leftChart  = theme(ChartPie.createYearlyPieChart(yearly, year));
                        rightChart = theme(ChartBar.createCategoryBarChart(yearly, "Yearly Overview", "Category", "Amount (RM)"));
                    }
                    dbOk = true; // signal success to done()
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // dbOk remains false → done() shows error/placeholder state
                }
                return null;
            }

            @Override
            protected void done() {
                if (isCancelled()) return; // a newer worker is already running — do nothing

                if (!dbOk) {
                    // ── No DB / error state: clear values and show placeholder ──
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
                    showNoDbPlaceholder(); // adds a centred error label to chartLeftPanel
                    updateCategoryBreakdown(null);
                } else {
                    // ── Success state: populate stat cards ────────────────────
                    currentMonthSpendingLabel.setText(String.format("RM %.2f", monthlyTotal));
                    averageExpensesLabel.setText(String.format("RM %.2f", avgMonthly));
                    yearlyTotalLabel.setText(String.format("RM %.2f", yearlyTotal));
                    monthChangeLabel.setText(monthChange != 0
                            ? String.format("%+.1f%%", monthChange) : "—");

                    // Colour the "vs Last Month" card: green = spent less, red = spent more
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
                    monthChangeIconCircle.repaint(); // triggers the dynamic colour fill

                    // ── Atomically swap charts ────────────────────────────────
                    // Remove old ChartFrame children and add the freshly-built ones.
                    // Old content stays visible until this EDT pass finishes, so
                    // there's no blank-frame flash.
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

                // Force layout + repaint regardless of success/failure
                chartLeftPanel.revalidate();  chartLeftPanel.repaint();
                chartRightPanel.revalidate(); chartRightPanel.repaint();
            }
        };
        pendingWorker.execute();
    }

    /**
     * Inserts a centred "Database connection required." label into chartLeftPanel.
     * Only called when the DB is unavailable (dbOk == false in the worker).
     */
    private void showNoDbPlaceholder() {
        JLabel err = new JLabel("Database connection required.");
        err.setForeground(LABEL_3);
        err.setFont(sf(Font.PLAIN, 12f));
        err.setHorizontalAlignment(JLabel.CENTER);
        chartLeftPanel.add(err, BorderLayout.CENTER);
    }

    /**
     * Applies the dashboard's colour theme to a JFreeChart object.
     * Called on every chart before it is handed to a ChartFrame.
     * Handles both PiePlot (no axes) and CategoryPlot (bar/line).
     */
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
        // Axis styling is only relevant for CategoryPlot (bar / line charts)
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
    // REUSABLE UI HELPERS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Creates a white rounded card panel with a subtle drop shadow.
     * Used for stat cards, chart panels, and the breakdown card.
     * Children are added AFTER this method returns by the caller.
     */
    private JPanel createCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Multi-pass diffuse shadow: 5 concentric semi-transparent fills
                for (int i = 5; i >= 1; i--) {
                    g2.setColor(new Color(0, 0, 0, 5));
                    g2.fillRoundRect(i, i+1, getWidth()-i*2, getHeight()-i*2, R+i, R+i);
                }
                // White card surface
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, R, R);
                g2.dispose();
            }
        };
        card.setOpaque(false); // let the manual paint handle the background
        card.setBorder(BorderFactory.createCompoundBorder(
            new MacBorder(SEP_LIGHT, R),                        // hairline border
            BorderFactory.createEmptyBorder(14, 16, 14, 16)     // inner padding
        ));
        return card;
    }

    // ── Stat-card label factory methods ──────────────────────────────────────
    // Each method creates a JLabel, stores it in the corresponding field,
    // and returns it so buildStatCard() can receive it inline.
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

    /** Secondary-label style used inside forms (e.g. "Category", "Amount"). */
    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(sf(Font.PLAIN, 12f));
        l.setForeground(LABEL_2);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    /** Secondary-label style used inline in the toolbar (e.g. "Month", "Year"). */
    private JLabel smallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(sf(Font.PLAIN, 12f));
        l.setForeground(LABEL_2);
        return l;
    }

    /**
     * Creates a thin vertical or horizontal separator rule.
     *
     * @param w Width in pixels  (use 1 for a vertical rule)
     * @param h Height in pixels (use 1 for a horizontal rule)
     */
    private Component thinRule(int w, int h) {
        JPanel r = new JPanel();
        r.setBackground(SEP);
        r.setPreferredSize(new Dimension(w, h));
        r.setMaximumSize(new Dimension(w, h));
        return r;
    }

    /** Blue filled-rounded-rect button (primary action). */
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

    /** Grey filled-rounded-rect button (secondary / cancel action). */
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

    /**
     * Replaces the default Swing scrollbar UI with a slimmer, macOS-style one:
     * fully-rounded thumb, no arrow buttons, grey thumb on white track.
     */
    private void styleScrollBar(JScrollBar bar) {
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0xC6C6C8); trackColor = BG;
            }
            @Override protected JButton createDecreaseButton(int o) { return zero(); }
            @Override protected JButton createIncreaseButton(int o) { return zero(); }
            JButton zero() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0,0)); // zero-size → arrow buttons disappear
                return b;
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // MacBorder — a hairline 1-px rounded-rectangle border
    // Used instead of LineBorder because LineBorder clips at sharp corners;
    // RoundRectangle2D.Double draws a crisp anti-aliased arc.
    // ════════════════════════════════════════════════════════════════════════
    static class MacBorder extends AbstractBorder {
        private final Color color;
        private final int   radius;

        MacBorder(Color c, int r) { color = c; radius = r; }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            // Inset by 0.5px so the stroke lands exactly on the component boundary
            g2.draw(new RoundRectangle2D.Double(x+.5, y+.5, w-1, h-1, radius, radius));
            g2.dispose();
        }

        // 1-px insets on all sides — just enough for the hairline border
        @Override public Insets getBorderInsets(Component c)           { return new Insets(1,1,1,1); }
        @Override public Insets getBorderInsets(Component c, Insets i) { i.set(1,1,1,1); return i; }
    }

    public static void main(String[] args) {
        // Swing UIs must be created/modified on the Event Dispatch Thread
        SwingUtilities.invokeLater(ExpensesComputeApplication::new);
    }
}