package com.ex.calculate; // Package declaration for the calculate module

import chart.SQLConnection; // Import for SQL connection utility

import javax.swing.*; // Import for Swing GUI components
import javax.swing.border.AbstractBorder; // Import for custom border class
import java.awt.*; // Import for AWT graphics and layout
import java.awt.geom.RoundRectangle2D; // Import for rounded rectangle shapes
import java.io.*; // Import for file input/output operations
import java.sql.Connection; // Import for database connections
import java.sql.DriverManager; // Import for JDBC driver management
import java.util.Properties; // Import for properties file handling

public class SettingsPage extends JPanel { // Class definition extending JPanel for settings UI

    // ── colour / font tokens ─────────────────────────────────────────────────
    private static final Color BG        = new Color(0xf6f6f6); // Background color constant
    private static final Color CARD_BG   = Color.WHITE; // Card background color
    private static final Color SEP       = new Color(0xC6C6C8); // Separator color
    private static final Color SEP_LIGHT = new Color(0xE5E5EA); // Light separator color
    private static final Color LABEL     = new Color(0x1C1C1E); // Primary label color
    private static final Color LABEL_2   = new Color(0x6E6E73); // Secondary label color
    private static final Color LABEL_3   = new Color(0xAEAEB2); // Tertiary label color
    private static final Color ACCENT    = new Color(0x007AFF); // Accent color for buttons
    private static final Color GREEN     = new Color(0x34C759); // Success color
    private static final Color RED       = new Color(0xFF3B30); // Error color
    private static final Color ORANGE    = new Color(0xFF9500); // Warning color
    private static final int   R         = 10; // Border radius constant

    private static final String PROPS_FILE = "db.properties"; // Properties file name for database settings

    // ── Default connection values ────────────────────────────────────────────
    private static final String DEFAULT_URL  = "jdbc:mysql://localhost:3306/accountdb"; // Default JDBC URL for MySQL
    private static final String DEFAULT_USER = "root"; // Default database username
    private static final String DEFAULT_PASS = ""; // Default database password

    private static Font sf(int style, float size) { // Method to create system font with fallback
        for (String n : new String[]{".SF NS Display", ".SF NS Text", "Helvetica Neue", "Helvetica", "SansSerif"}) { // Loop through preferred font names
            Font f = new Font(n, style, (int) size); // Create font with current name
            if (!f.getFamily().equals("Dialog")) return f.deriveFont(size); // Return derived font if not default
        }
        return new Font("SansSerif", style, (int) size); // Fallback to SansSerif
    }

    // ── form fields ──────────────────────────────────────────────────────────
    private JTextField    urlField; // Text field for JDBC URL input
    private JTextField    userField; // Text field for username input
    private JPasswordField passField; // Password field for password input
    private JLabel        statusLabel; // Label to display connection status
    private JPanel        statusDot; // Panel for status indicator dot

    // ─────────────────────────────────────────────────────────────────────────
    public SettingsPage() { // Constructor for SettingsPage
        setLayout(new BorderLayout()); // Set layout to BorderLayout
        setBackground(BG); // Set background color
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22)); // Set padding border
        add(buildHeader(), BorderLayout.NORTH); // Add header panel to north
        add(buildForm(),   BorderLayout.CENTER); // Add form panel to center
        loadSavedSettings(); // Load previously saved settings
    }

    // ════════════════════════════════════════════════════════════════════════
    // HEADER
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() { // Method to build the header panel
        JPanel header = new JPanel(new BorderLayout()); // Create header panel with BorderLayout
        header.setOpaque(false); // Make header transparent
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0)); // Set bottom margin

        JPanel left = new JPanel(); // Create left panel for title and subtitle
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); // Set vertical box layout
        left.setOpaque(false); // Make left panel transparent

        JLabel title = new JLabel("Settings"); // Create title label
        title.setFont(sf(Font.BOLD, 20f)); // Set bold font for title
        title.setForeground(LABEL); // Set title color

        JLabel sub = new JLabel("Manage your database connection"); // Create subtitle label
        sub.setFont(sf(Font.PLAIN, 12f)); // Set plain font for subtitle
        sub.setForeground(LABEL_2); // Set subtitle color

        left.add(title); // Add title to left panel
        left.add(Box.createVerticalStrut(2)); // Add small vertical space
        left.add(sub); // Add subtitle to left panel

        header.add(left, BorderLayout.WEST); // Add left panel to west of header
        return header; // Return the header panel
    }

    // ════════════════════════════════════════════════════════════════════════
    // FORM CARD
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildForm() { // Method to build the main form panel
        JPanel wrapper = new JPanel(new BorderLayout()); // Create wrapper panel with BorderLayout
        wrapper.setOpaque(false); // Make wrapper transparent

        JPanel card = createCard(); // Create the card panel using helper method
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); // Set vertical box layout for card
        card.setBorder(BorderFactory.createCompoundBorder( // Set compound border with round border and padding
            new RoundBorder(SEP_LIGHT, R),
            BorderFactory.createEmptyBorder(28, 28, 28, 28)
        ));

        // ── Section heading ──────────────────────────────────────────────────
        JLabel sectionTitle = new JLabel("Database Connection"); // Create section title label
        sectionTitle.setFont(sf(Font.BOLD, 15f)); // Set bold font for section title
        sectionTitle.setForeground(LABEL); // Set title color
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        card.add(sectionTitle); // Add title to card

        card.add(Box.createVerticalStrut(4)); // Add small vertical space

        JLabel hint = new JLabel("Changes take effect immediately after saving."); // Create hint label
        hint.setFont(sf(Font.PLAIN, 12f)); // Set plain font for hint
        hint.setForeground(LABEL_3); // Set hint color
        hint.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        card.add(hint); // Add hint to card

        card.add(Box.createVerticalStrut(22)); // Add vertical space
        card.add(hairline()); // Add horizontal line separator
        card.add(Box.createVerticalStrut(22)); // Add vertical space

        // ── JDBC URL ─────────────────────────────────────────────────────────
        card.add(fieldLabel("JDBC URL")); // Add field label for JDBC URL
        card.add(Box.createVerticalStrut(6)); // Add small vertical space
        urlField = styledField(DEFAULT_URL); // Create styled text field for URL with default
        card.add(urlField); // Add URL field to card

        card.add(Box.createVerticalStrut(4)); // Add small vertical space
        JLabel urlHint = new JLabel("  MySQL: jdbc:mysql://HOST:PORT/DB  |  Oracle: jdbc:oracle:thin:@HOST:PORT:SID"); // Create URL hint label
        urlHint.setFont(sf(Font.PLAIN, 11f)); // Set font for hint
        urlHint.setForeground(LABEL_3); // Set hint color
        urlHint.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        card.add(urlHint); // Add hint to card
        card.add(Box.createVerticalStrut(16)); // Add vertical space

        // ── Username ─────────────────────────────────────────────────────────
        card.add(fieldLabel("Username")); // Add field label for username
        card.add(Box.createVerticalStrut(6)); // Add small vertical space
        userField = styledField(DEFAULT_USER); // Create styled text field for username with default
        card.add(userField); // Add username field to card
        card.add(Box.createVerticalStrut(16)); // Add vertical space

        // ── Password ─────────────────────────────────────────────────────────
        card.add(fieldLabel("Password")); // Add field label for password
        card.add(Box.createVerticalStrut(6)); // Add small vertical space
        passField = new JPasswordField(); // Create password field
        passField.setFont(sf(Font.PLAIN, 13f)); // Set font for password field
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); // Set max size
        passField.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        passField.setBackground(new Color(0xF9F9FB)); // Set background color
        passField.setBorder(BorderFactory.createCompoundBorder( // Set compound border
            new RoundBorder(SEP, 8),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        card.add(passField); // Add password field to card
        card.add(Box.createVerticalStrut(28)); // Add vertical space

        // ── Status indicator ─────────────────────────────────────────────────
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); // Create status row panel
        statusRow.setOpaque(false); // Make transparent
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        statusRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24)); // Set max size

        statusDot = new JPanel() { // Create status dot panel with custom painting
            @Override protected void paintComponent(Graphics g) { // Override paint method
                Graphics2D g2 = (Graphics2D) g.create(); // Create graphics context
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Enable antialiasing
                g2.setColor(getBackground()); // Set color to background
                g2.fillOval(0, 3, 10, 10); // Draw filled oval
                g2.dispose(); // Dispose graphics
            }
            @Override public Dimension getPreferredSize() { return new Dimension(10, 16); } // Override preferred size
        };
        statusDot.setOpaque(false); // Make transparent
        statusDot.setBackground(LABEL_3);   // grey = untested // Set initial background color

        statusLabel = new JLabel("Not tested yet"); // Create status label
        statusLabel.setFont(sf(Font.PLAIN, 12f)); // Set font
        statusLabel.setForeground(LABEL_2); // Set color

        statusRow.add(statusDot); // Add dot to status row
        statusRow.add(statusLabel); // Add label to status row
        card.add(statusRow); // Add status row to card
        card.add(Box.createVerticalStrut(18)); // Add vertical space

        // ── Button row ───────────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); // Create button row panel
        btnRow.setOpaque(false); // Make transparent
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38)); // Set max size

        JButton testBtn  = buildBtn("Test Connection",  new Color(0xE5E5EA), LABEL); // Create test button
        JButton saveBtn  = buildBtn("Save & Apply",     ACCENT,              Color.WHITE); // Create save button
        JButton resetBtn = buildBtn("Reset to Default", new Color(0xFFE5E4), RED); // Create reset button

        testBtn.addActionListener(e  -> testConnection()); // Add action listener for test
        saveBtn.addActionListener(e  -> saveAndApply()); // Add action listener for save
        resetBtn.addActionListener(e -> resetDefaults()); // Add action listener for reset

        btnRow.add(testBtn); // Add test button
        btnRow.add(saveBtn); // Add save button
        btnRow.add(resetBtn); // Add reset button
        card.add(btnRow); // Add button row to card

        // ── Info box ─────────────────────────────────────────────────────────
        card.add(Box.createVerticalStrut(28)); // Add vertical space
        card.add(hairline()); // Add horizontal line
        card.add(Box.createVerticalStrut(18)); // Add vertical space
        card.add(buildInfoBox()); // Add info box

        card.setMaximumSize(new Dimension(680, Integer.MAX_VALUE)); // Set max size for card
        card.setPreferredSize(new Dimension(620, 500)); // Set preferred size

        wrapper.add(card, BorderLayout.CENTER); // Add card to wrapper center
        return wrapper; // Return the wrapper panel
    }

    // ── Information box at the bottom of the card ────────────────────────────
    private JPanel buildInfoBox() { // Method to build the information box panel
        JPanel box = new JPanel(new BorderLayout(10, 0)) { // Create box panel with BorderLayout and gap
            @Override protected void paintComponent(Graphics g) { // Override paint method for custom background
                Graphics2D g2 = (Graphics2D) g.create(); // Create graphics context
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Enable antialiasing
                g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 15)); // Set semi-transparent accent color
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); // Fill rounded rectangle
                g2.dispose(); // Dispose graphics
            }
        };
        box.setOpaque(false); // Make box transparent
        box.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80)); // Set max size
        box.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14)); // Set padding

        JLabel icon = new JLabel("ℹ"); // Create info icon label
        icon.setFont(sf(Font.PLAIN, 16f)); // Set font for icon
        icon.setForeground(ACCENT); // Set icon color

        JLabel text = new JLabel("<html>" // Create HTML text label
            + "<b>Tip:</b> Settings are saved to <tt>db.properties</tt> beside the application JAR. "
            + "The application will reconnect automatically after saving. "
            + "Make sure your JDBC driver (MySQL or Oracle) is on the classpath."
            + "</html>");
        text.setFont(sf(Font.PLAIN, 11.5f)); // Set font for text
        text.setForeground(LABEL_2); // Set text color

        box.add(icon, BorderLayout.WEST); // Add icon to west
        box.add(text, BorderLayout.CENTER); // Add text to center
        return box; // Return the box panel
    }

    // ════════════════════════════════════════════════════════════════════════
    // ACTIONS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Tests the connection using the credentials currently in the form fields,
     * without saving anything. Detects the correct JDBC driver from the URL.
     */
    private void testConnection() { // Method to test database connection
        String url  = urlField.getText().trim(); // Get trimmed URL from field
        String user = userField.getText().trim(); // Get trimmed username from field
        String pass = new String(passField.getPassword()); // Get password from field

        if (url.isEmpty() || user.isEmpty()) { // Check if URL or user is empty
            setStatus(ORANGE, "URL and Username cannot be empty"); // Set warning status
            return; // Exit method
        }

        setStatus(ORANGE, "Testing…"); // Set testing status

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() { // Create background worker for connection test
            String errorMsg = ""; // Variable to hold error message

            @Override
            protected Boolean doInBackground() { // Background task method
                try {
                    loadDriver(url); // Load appropriate JDBC driver
                    DriverManager.setLoginTimeout(5); // Set login timeout to 5 seconds
                    Connection c = DriverManager.getConnection(url, user, pass); // Attempt connection
                    c.close(); // Close connection
                    return true; // Return success
                } catch (Exception e) {
                    errorMsg = e.getMessage(); // Capture error message
                    return false; // Return failure
                }
            }

            @Override
            protected void done() { // Method called when background task completes
                try {
                    if (get()) { // If successful
                        setStatus(GREEN, "Connection successful ✓"); // Set success status
                    } else {
                        setStatus(RED, "Connection failed — " + shorten(errorMsg, 80)); // Set failure status with shortened error
                    }
                } catch (Exception ex) {
                    setStatus(RED, "Error: " + shorten(ex.getMessage(), 80)); // Set error status
                }
            }
        };
        worker.execute(); // Start the background worker
    }

    /**
     * Validates inputs, saves credentials to db.properties, resets the
     * SQLConnection singleton, and reconnects in a background thread.
     */
    private void saveAndApply() { // Method to save settings and apply connection
        String url  = urlField.getText().trim(); // Get trimmed URL
        String user = userField.getText().trim(); // Get trimmed username
        String pass = new String(passField.getPassword()); // Get password

        if (url.isEmpty() || user.isEmpty()) { // Validate inputs
            JOptionPane.showMessageDialog(this, // Show warning dialog
                "URL and Username cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return; // Exit
        }

        // 1. Persist credentials to file
        Properties props = new Properties(); // Create properties object
        props.setProperty("db.url",      url); // Set URL property
        props.setProperty("db.user",     user); // Set user property
        props.setProperty("db.password", pass); // Set password property
        try (FileOutputStream fos = new FileOutputStream(PROPS_FILE)) { // Open file output stream
            props.store(fos, "Account Dashboard — DB connection settings"); // Store properties to file
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, // Show error dialog
                "Could not write " + PROPS_FILE + ":\n" + e.getMessage(),
                "Save Error", JOptionPane.ERROR_MESSAGE);
            return; // Exit
        }

        // 2. Reset the singleton so the next call creates a fresh connection
        SQLConnection.reset(); // Reset SQL connection singleton

        setStatus(ORANGE, "Settings saved. Reconnecting…"); // Set reconnecting status

        // 3. Reconnect in background so the UI doesn't freeze
        new Thread(() -> { // Start background thread for reconnection
            try {
                Connection conn = SQLConnection.getInstance().getConnection(); // Get new connection

                SwingUtilities.invokeLater(() -> { // Update UI on EDT
                    if (conn != null) { // If connection successful
                        setStatus(GREEN, "Connected ✓"); // Set success status

                        // Notify the parent window to refresh its charts/data
                        Window parentWindow = SwingUtilities.getWindowAncestor(SettingsPage.this); // Get parent window
                        if (parentWindow instanceof ExpensesComputeApplication) { // Check if it's the main app
                            ((ExpensesComputeApplication) parentWindow).updateCharts(); // Update charts
                        }
                    } else {
                        setStatus(RED, "Reconnect failed — connection returned null"); // Set failure status
                    }
                });

            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> // Update UI on EDT
                    setStatus(RED, "Reconnect failed — " + shorten(e.getMessage(), 70)) // Set error status
                );
            }
        }, "db-reconnect-thread").start(); // Start thread with name
    }

    /**
     * Resets all form fields back to the compiled-in MySQL defaults.
     * Does NOT save or reconnect — user must click "Save & Apply" afterwards.
     */
    private void resetDefaults() { // Method to reset form fields to defaults
        int confirm = JOptionPane.showConfirmDialog(this, // Show confirmation dialog
            "Reset to default MySQL settings?",
            "Reset", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return; // If not yes, exit

        urlField.setText(DEFAULT_URL); // Set URL field to default
        userField.setText(DEFAULT_USER); // Set user field to default
        passField.setText(DEFAULT_PASS); // Set password field to default
        setStatus(LABEL_3, "Reset to defaults — not saved"); // Set status message
    }

    // ════════════════════════════════════════════════════════════════════════
    // PERSISTENCE
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Reads db.properties and pre-fills the form fields on startup.
     * Falls back to compiled-in defaults if the file doesn't exist.
     */
    private void loadSavedSettings() { // Method to load saved settings from file
        File f = new File(PROPS_FILE); // Create file object for properties file
        if (!f.exists()) { // If file doesn't exist
            urlField.setText(DEFAULT_URL); // Set URL to default
            userField.setText(DEFAULT_USER); // Set user to default
            passField.setText(DEFAULT_PASS); // Set password to default
            return; // Exit
        }

        Properties props = new Properties(); // Create properties object
        try (FileInputStream fis = new FileInputStream(f)) { // Open file input stream
            props.load(fis); // Load properties from file
            urlField.setText(props.getProperty("db.url",      DEFAULT_URL)); // Set URL from properties or default
            userField.setText(props.getProperty("db.user",    DEFAULT_USER)); // Set user from properties or default
            passField.setText(props.getProperty("db.password", DEFAULT_PASS)); // Set password from properties or default
            setStatus(ACCENT, "Loaded from db.properties"); // Set loaded status
        } catch (IOException e) {
            setStatus(ORANGE, "Could not read saved settings"); // Set error status
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Loads the correct JDBC driver class based on the URL prefix.
     * Matches the detection logic in SQLConnection.loadDriver().
     */
    private void loadDriver(String url) throws ClassNotFoundException { // Method to load JDBC driver based on URL
        if (url == null || url.isEmpty()) { // Check if URL is null or empty
            throw new ClassNotFoundException("Database URL is empty."); // Throw exception
        }
        if (url.startsWith("jdbc:mysql")) { // If MySQL URL
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load MySQL driver
        } else if (url.startsWith("jdbc:oracle")) { // If Oracle URL
            Class.forName("oracle.jdbc.OracleDriver"); // Load Oracle driver
        } else {
            throw new ClassNotFoundException("Unsupported DB type: " + url); // Throw exception for unsupported
        }
    }

    private void setStatus(Color dotColor, String message) { // Method to set status indicator and message
        statusDot.setBackground(dotColor); // Set dot background color
        statusDot.repaint(); // Repaint the dot
        statusLabel.setText(message); // Set status label text
        statusLabel.setForeground(dotColor.equals(LABEL_3) ? LABEL_2 : dotColor); // Set label color based on dot color
    }

    private JLabel fieldLabel(String text) { // Method to create a styled field label
        JLabel l = new JLabel(text); // Create label with text
        l.setFont(sf(Font.PLAIN, 12f)); // Set font
        l.setForeground(LABEL_2); // Set color
        l.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        return l; // Return label
    }

    private JTextField styledField(String placeholder) { // Method to create a styled text field with placeholder
        JTextField tf = new JTextField() { // Create custom text field
            @Override protected void paintComponent(Graphics g) { // Override paint for placeholder
                super.paintComponent(g); // Call super
                if (getText().isEmpty() && !isFocusOwner()) { // If empty and not focused
                    Graphics2D g2 = (Graphics2D) g.create(); // Create graphics
                    g2.setColor(new Color(0xAEAEB2)); // Set placeholder color
                    g2.setFont(getFont()); // Set font
                    g2.drawString(placeholder, 10, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2); // Draw placeholder
                    g2.dispose(); // Dispose
                }
            }
        };
        tf.setFont(sf(Font.PLAIN, 13f)); // Set font
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); // Set max size
        tf.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        tf.setBackground(new Color(0xF9F9FB)); // Set background color
        tf.setBorder(BorderFactory.createCompoundBorder( // Set compound border
            new RoundBorder(SEP, 8),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return tf; // Return text field
    }

    private JPanel hairline() { // Method to create a thin horizontal line separator
        JPanel line = new JPanel(); // Create panel for line
        line.setBackground(SEP_LIGHT); // Set background color
        line.setPreferredSize(new Dimension(Integer.MAX_VALUE, 1)); // Set preferred size
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1)); // Set max size
        line.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        return line; // Return line panel
    }

    private JPanel createCard() { // Method to create a card panel with shadow effect
        JPanel card = new JPanel() { // Create custom panel
            @Override protected void paintComponent(Graphics g) { // Override paint for shadow
                Graphics2D g2 = (Graphics2D) g.create(); // Create graphics
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Enable antialiasing
                for (int i = 5; i >= 1; i--) { // Loop for shadow layers
                    g2.setColor(new Color(0, 0, 0, 5)); // Set shadow color
                    g2.fillRoundRect(i, i + 1, getWidth() - i * 2, getHeight() - i * 2, R + i, R + i); // Draw shadow
                }
                g2.setColor(CARD_BG); // Set card background color
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, R, R); // Fill card background
                g2.dispose(); // Dispose
            }
        };
        card.setOpaque(false); // Make transparent
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); // Set vertical box layout
        return card; // Return card panel
    }

    private JButton buildBtn(String text, Color bg, Color fg) { // Method to create a styled button
        JButton btn = new JButton(text) { // Create custom button
            @Override protected void paintComponent(Graphics g) { // Override paint for custom look
                Graphics2D g2 = (Graphics2D) g.create(); // Create graphics
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Enable antialiasing
                g2.setColor(getModel().isPressed() ? bg.darker() : bg); // Set color based on pressed state
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8); // Fill rounded rectangle
                g2.setFont(getFont()); // Set font
                g2.setColor(fg); // Set text color
                FontMetrics fm = g2.getFontMetrics(); // Get font metrics
                g2.drawString(getText(), // Draw text centered
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose(); // Dispose
            }
        };
        btn.setFont(sf(Font.PLAIN, 13f)); // Set font
        btn.setPreferredSize(new Dimension(148, 32)); // Set preferred size
        btn.setContentAreaFilled(false); // Disable default content fill
        btn.setBorderPainted(false); // Disable border painting
        btn.setFocusPainted(false); // Disable focus painting
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Set hand cursor
        return btn; // Return button
    }

    private String shorten(String s, int max) { // Method to shorten string if too long
        if (s == null) return ""; // Return empty if null
        return s.length() > max ? s.substring(0, max) + "…" : s; // Return shortened with ellipsis or original
    }

    // ════════════════════════════════════════════════════════════════════════
    // INNER CLASS — RoundBorder
    // ════════════════════════════════════════════════════════════════════════
    static class RoundBorder extends AbstractBorder { // Inner class for rounded border
        private final Color color; // Border color
        private final int   radius; // Border radius

        RoundBorder(Color c, int r) { color = c; radius = r; } // Constructor

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) { // Paint the border
            Graphics2D g2 = (Graphics2D) g.create(); // Create graphics
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Enable antialiasing
            g2.setColor(color); // Set color
            g2.draw(new RoundRectangle2D.Double(x + .5, y + .5, w - 1, h - 1, radius, radius)); // Draw rounded rectangle
            g2.dispose(); // Dispose
        }

        @Override public Insets getBorderInsets(Component c)           { return new Insets(1, 1, 1, 1); } // Return insets
        @Override public Insets getBorderInsets(Component c, Insets i) { i.set(1, 1, 1, 1); return i; } // Return insets with modification
    }
} // End of SettingsPage class