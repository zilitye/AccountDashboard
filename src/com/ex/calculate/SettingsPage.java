package com.ex.calculate;

import chart.SQLConnection;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class SettingsPage extends JPanel {

    // ── colour / font tokens ─────────────────────────────────────────────────
    private static final Color BG        = new Color(0xf6f6f6);
    private static final Color CARD_BG   = Color.WHITE;
    private static final Color SEP       = new Color(0xC6C6C8);
    private static final Color SEP_LIGHT = new Color(0xE5E5EA);
    private static final Color LABEL     = new Color(0x1C1C1E);
    private static final Color LABEL_2   = new Color(0x6E6E73);
    private static final Color LABEL_3   = new Color(0xAEAEB2);
    private static final Color ACCENT    = new Color(0x007AFF);
    private static final Color GREEN     = new Color(0x34C759);
    private static final Color RED       = new Color(0xFF3B30);
    private static final Color ORANGE    = new Color(0xFF9500);
    private static final int   R         = 10;

    private static final String PROPS_FILE = "db.properties";

    // ── Default connection values ────────────────────────────────────────────
    private static final String DEFAULT_URL  = "jdbc:mysql://localhost:3306/accountdb";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    private static Font sf(int style, float size) {
        for (String n : new String[]{".SF NS Display", ".SF NS Text", "Helvetica Neue", "Helvetica", "SansSerif"}) {
            Font f = new Font(n, style, (int) size);
            if (!f.getFamily().equals("Dialog")) return f.deriveFont(size);
        }
        return new Font("SansSerif", style, (int) size);
    }

    // ── form fields ──────────────────────────────────────────────────────────
    private JTextField    urlField;
    private JTextField    userField;
    private JPasswordField passField;
    private JLabel        statusLabel;
    private JPanel        statusDot;

    // ─────────────────────────────────────────────────────────────────────────
    public SettingsPage() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(),   BorderLayout.CENTER);
        loadSavedSettings();
    }

    // ════════════════════════════════════════════════════════════════════════
    // HEADER
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("Settings");
        title.setFont(sf(Font.BOLD, 20f));
        title.setForeground(LABEL);

        JLabel sub = new JLabel("Manage your database connection");
        sub.setFont(sf(Font.PLAIN, 12f));
        sub.setForeground(LABEL_2);

        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(sub);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    // ════════════════════════════════════════════════════════════════════════
    // FORM CARD
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(SEP_LIGHT, R),
            BorderFactory.createEmptyBorder(28, 28, 28, 28)
        ));

        // ── Section heading ──────────────────────────────────────────────────
        JLabel sectionTitle = new JLabel("Database Connection");
        sectionTitle.setFont(sf(Font.BOLD, 15f));
        sectionTitle.setForeground(LABEL);
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sectionTitle);

        card.add(Box.createVerticalStrut(4));

        JLabel hint = new JLabel("Changes take effect immediately after saving.");
        hint.setFont(sf(Font.PLAIN, 12f));
        hint.setForeground(LABEL_3);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(hint);

        card.add(Box.createVerticalStrut(22));
        card.add(hairline());
        card.add(Box.createVerticalStrut(22));

        // ── JDBC URL ─────────────────────────────────────────────────────────
        card.add(fieldLabel("JDBC URL"));
        card.add(Box.createVerticalStrut(6));
        urlField = styledField(DEFAULT_URL);
        card.add(urlField);

        card.add(Box.createVerticalStrut(4));
        JLabel urlHint = new JLabel("  MySQL: jdbc:mysql://HOST:PORT/DB  |  Oracle: jdbc:oracle:thin:@HOST:PORT:SID");
        urlHint.setFont(sf(Font.PLAIN, 11f));
        urlHint.setForeground(LABEL_3);
        urlHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(urlHint);
        card.add(Box.createVerticalStrut(16));

        // ── Username ─────────────────────────────────────────────────────────
        card.add(fieldLabel("Username"));
        card.add(Box.createVerticalStrut(6));
        userField = styledField(DEFAULT_USER);
        card.add(userField);
        card.add(Box.createVerticalStrut(16));

        // ── Password ─────────────────────────────────────────────────────────
        card.add(fieldLabel("Password"));
        card.add(Box.createVerticalStrut(6));
        passField = new JPasswordField();
        passField.setFont(sf(Font.PLAIN, 13f));
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passField.setBackground(new Color(0xF9F9FB));
        passField.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(SEP, 8),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        card.add(passField);
        card.add(Box.createVerticalStrut(28));

        // ── Status indicator ─────────────────────────────────────────────────
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        statusRow.setOpaque(false);
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        statusDot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 3, 10, 10);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(10, 16); }
        };
        statusDot.setOpaque(false);
        statusDot.setBackground(LABEL_3);   // grey = untested

        statusLabel = new JLabel("Not tested yet");
        statusLabel.setFont(sf(Font.PLAIN, 12f));
        statusLabel.setForeground(LABEL_2);

        statusRow.add(statusDot);
        statusRow.add(statusLabel);
        card.add(statusRow);
        card.add(Box.createVerticalStrut(18));

        // ── Button row ───────────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JButton testBtn  = buildBtn("Test Connection",  new Color(0xE5E5EA), LABEL);
        JButton saveBtn  = buildBtn("Save & Apply",     ACCENT,              Color.WHITE);
        JButton resetBtn = buildBtn("Reset to Default", new Color(0xFFE5E4), RED);

        testBtn.addActionListener(e  -> testConnection());
        saveBtn.addActionListener(e  -> saveAndApply());
        resetBtn.addActionListener(e -> resetDefaults());

        btnRow.add(testBtn);
        btnRow.add(saveBtn);
        btnRow.add(resetBtn);
        card.add(btnRow);

        // ── Info box ─────────────────────────────────────────────────────────
        card.add(Box.createVerticalStrut(28));
        card.add(hairline());
        card.add(Box.createVerticalStrut(18));
        card.add(buildInfoBox());

        card.setMaximumSize(new Dimension(680, Integer.MAX_VALUE));
        card.setPreferredSize(new Dimension(620, 500));

        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ── Information box at the bottom of the card ────────────────────────────
    private JPanel buildInfoBox() {
        JPanel box = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        box.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JLabel icon = new JLabel("ℹ");
        icon.setFont(sf(Font.PLAIN, 16f));
        icon.setForeground(ACCENT);

        JLabel text = new JLabel("<html>"
            + "<b>Tip:</b> Settings are saved to <tt>db.properties</tt> beside the application JAR. "
            + "The application will reconnect automatically after saving. "
            + "Make sure your JDBC driver (MySQL or Oracle) is on the classpath."
            + "</html>");
        text.setFont(sf(Font.PLAIN, 11.5f));
        text.setForeground(LABEL_2);

        box.add(icon, BorderLayout.WEST);
        box.add(text, BorderLayout.CENTER);
        return box;
    }

    // ════════════════════════════════════════════════════════════════════════
    // ACTIONS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Tests the connection using the credentials currently in the form fields,
     * without saving anything. Detects the correct JDBC driver from the URL.
     */
    private void testConnection() {
        String url  = urlField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        if (url.isEmpty() || user.isEmpty()) {
            setStatus(ORANGE, "URL and Username cannot be empty");
            return;
        }

        setStatus(ORANGE, "Testing…");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            String errorMsg = "";

            @Override
            protected Boolean doInBackground() {
                try {
                    loadDriver(url);
                    DriverManager.setLoginTimeout(5);
                    Connection c = DriverManager.getConnection(url, user, pass);
                    c.close();
                    return true;
                } catch (Exception e) {
                    errorMsg = e.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        setStatus(GREEN, "Connection successful ✓");
                    } else {
                        setStatus(RED, "Connection failed — " + shorten(errorMsg, 80));
                    }
                } catch (Exception ex) {
                    setStatus(RED, "Error: " + shorten(ex.getMessage(), 80));
                }
            }
        };
        worker.execute();
    }

    /**
     * Validates inputs, saves credentials to db.properties, resets the
     * SQLConnection singleton, and reconnects in a background thread.
     */
    private void saveAndApply() {
        String url  = urlField.getText().trim();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());

        if (url.isEmpty() || user.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "URL and Username cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Persist credentials to file
        Properties props = new Properties();
        props.setProperty("db.url",      url);
        props.setProperty("db.user",     user);
        props.setProperty("db.password", pass);
        try (FileOutputStream fos = new FileOutputStream(PROPS_FILE)) {
            props.store(fos, "Account Dashboard — DB connection settings");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Could not write " + PROPS_FILE + ":\n" + e.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Reset the singleton so the next call creates a fresh connection
        SQLConnection.reset();

        setStatus(ORANGE, "Settings saved. Reconnecting…");

        // 3. Reconnect in background so the UI doesn't freeze
        new Thread(() -> {
            try {
                Connection conn = SQLConnection.getInstance().getConnection();

                SwingUtilities.invokeLater(() -> {
                    if (conn != null) {
                        setStatus(GREEN, "Connected ✓");

                        // Notify the parent window to refresh its charts/data
                        Window parentWindow = SwingUtilities.getWindowAncestor(SettingsPage.this);
                        if (parentWindow instanceof ExpensesComputeApplication) {
                            ((ExpensesComputeApplication) parentWindow).updateCharts();
                        }
                    } else {
                        setStatus(RED, "Reconnect failed — connection returned null");
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                    setStatus(RED, "Reconnect failed — " + shorten(e.getMessage(), 70))
                );
            }
        }, "db-reconnect-thread").start();
    }

    /**
     * Resets all form fields back to the compiled-in MySQL defaults.
     * Does NOT save or reconnect — user must click "Save & Apply" afterwards.
     */
    private void resetDefaults() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Reset to default MySQL settings?",
            "Reset", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        urlField.setText(DEFAULT_URL);
        userField.setText(DEFAULT_USER);
        passField.setText(DEFAULT_PASS);
        setStatus(LABEL_3, "Reset to defaults — not saved");
    }

    // ════════════════════════════════════════════════════════════════════════
    // PERSISTENCE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Reads db.properties and pre-fills the form fields on startup.
     * Falls back to compiled-in defaults if the file doesn't exist.
     */
    private void loadSavedSettings() {
        File f = new File(PROPS_FILE);
        if (!f.exists()) {
            urlField.setText(DEFAULT_URL);
            userField.setText(DEFAULT_USER);
            passField.setText(DEFAULT_PASS);
            return;
        }

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(f)) {
            props.load(fis);
            urlField.setText(props.getProperty("db.url",      DEFAULT_URL));
            userField.setText(props.getProperty("db.user",    DEFAULT_USER));
            passField.setText(props.getProperty("db.password", DEFAULT_PASS));
            setStatus(ACCENT, "Loaded from db.properties");
        } catch (IOException e) {
            setStatus(ORANGE, "Could not read saved settings");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Loads the correct JDBC driver class based on the URL prefix.
     * Matches the detection logic in SQLConnection.loadDriver().
     */
    private void loadDriver(String url) throws ClassNotFoundException {
        if (url == null || url.isEmpty()) {
            throw new ClassNotFoundException("Database URL is empty.");
        }
        if (url.startsWith("jdbc:mysql")) {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } else if (url.startsWith("jdbc:oracle")) {
            Class.forName("oracle.jdbc.OracleDriver");
        } else {
            throw new ClassNotFoundException("Unsupported DB type: " + url);
        }
    }

    private void setStatus(Color dotColor, String message) {
        statusDot.setBackground(dotColor);
        statusDot.repaint();
        statusLabel.setText(message);
        statusLabel.setForeground(dotColor.equals(LABEL_3) ? LABEL_2 : dotColor);
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(sf(Font.PLAIN, 12f));
        l.setForeground(LABEL_2);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField styledField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(0xAEAEB2));
                    g2.setFont(getFont());
                    g2.drawString(placeholder, 10, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
                    g2.dispose();
                }
            }
        };
        tf.setFont(sf(Font.PLAIN, 13f));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        tf.setBackground(new Color(0xF9F9FB));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(SEP, 8),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    private JPanel hairline() {
        JPanel line = new JPanel();
        line.setBackground(SEP_LIGHT);
        line.setPreferredSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }

    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 5; i >= 1; i--) {
                    g2.setColor(new Color(0, 0, 0, 5));
                    g2.fillRoundRect(i, i + 1, getWidth() - i * 2, getHeight() - i * 2, R + i, R + i);
                }
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, R, R);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        return card;
    }

    private JButton buildBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setFont(getFont());
                g2.setColor(fg);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(sf(Font.PLAIN, 13f));
        btn.setPreferredSize(new Dimension(148, 32));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private String shorten(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    // ════════════════════════════════════════════════════════════════════════
    // INNER CLASS — RoundBorder
    // ════════════════════════════════════════════════════════════════════════
    static class RoundBorder extends AbstractBorder {
        private final Color color;
        private final int   radius;

        RoundBorder(Color c, int r) { color = c; radius = r; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.draw(new RoundRectangle2D.Double(x + .5, y + .5, w - 1, h - 1, radius, radius));
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c)           { return new Insets(1, 1, 1, 1); }
        @Override public Insets getBorderInsets(Component c, Insets i) { i.set(1, 1, 1, 1); return i; }
    }
}