package com.ex.calculate; // Declares the package name for organization

import javax.swing.*; // Imports Swing UI components
import javax.swing.border.AbstractBorder; // Imports abstract class for custom borders
import java.awt.*; // Imports AWT graphics and layout classes
import java.awt.geom.RoundRectangle2D; // Imports rounded rectangle shape
import java.io.*; // Imports file input/output classes
import java.util.Properties; // Imports Properties class for config files

public class SettingsPage extends JPanel { // Settings page panel class

    // ── colour / font tokens ─────────────────────────────────────────────────
    private static final Color BG        = new Color(0xf6f6f6); // Main background color
    private static final Color CARD_BG   = Color.WHITE; // Card background color
    private static final Color SEP       = new Color(0xC6C6C8); // Separator color
    private static final Color SEP_LIGHT = new Color(0xE5E5EA); // Light separator color
    private static final Color LABEL     = new Color(0x1C1C1E); // Primary text color
    private static final Color LABEL_2   = new Color(0x6E6E73); // Secondary text color
    private static final Color LABEL_3   = new Color(0xAEAEB2); // Tertiary text color
    private static final int   R         = 10; // Standard corner radius

    private static final String PROPS_FILE = "db.properties"; // Configuration file name

    // ── Default connection values ────────────────────────────────────────────
    private static final String DEFAULT_URL  = "jdbc:mysql://localhost:3306/accountdb"; // Default DB URL
    private static final String DEFAULT_USER = "root"; // Default database username
    private static final String DEFAULT_PASS = ""; // Default database password

    // Helper method to load macOS-style fonts
    private static Font sf(int style, float size) {
        // Try multiple font names until one works
        for (String n : new String[]{".SF NS Display", ".SF NS Text", "Helvetica Neue", "Helvetica", "SansSerif"}) {
            Font f = new Font(n, style, (int) size); // Create font candidate
            if (!f.getFamily().equals("Dialog")) return f.deriveFont(size); // Return valid font
        }
        return new Font("SansSerif", style, (int) size); // Fallback font
    }

    // ── form fields ──────────────────────────────────────────────────────────
    private JTextField    urlField; // Field for JDBC URL
    private JTextField    userField; // Field for username
    private JPasswordField passField; // Field for password
    private JLabel        statusLabel; // Status text label
    private JPanel        statusDot; // Colored status indicator dot

    // ─────────────────────────────────────────────────────────────────────────
    public SettingsPage() { // Constructor
        setLayout(new BorderLayout()); // Set BorderLayout
        setBackground(BG); // Set background color
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22)); // Add outer padding
        add(buildHeader(), BorderLayout.NORTH); // Add header panel
        add(buildForm(),   BorderLayout.CENTER); // Add form panel
        loadSavedSettings(); // Load saved DB settings
    }

    // ════════════════════════════════════════════════════════════════════════
    // HEADER
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() { // Creates top header panel
        JPanel header = new JPanel(new BorderLayout()); // Create header panel
        header.setOpaque(false); // Make transparent
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0)); // Bottom spacing

        JPanel left = new JPanel(); // Left content panel
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); // Vertical layout
        left.setOpaque(false); // Transparent background

        JLabel title = new JLabel("Settings"); // Main title label
        title.setFont(sf(Font.BOLD, 20f)); // Bold large font
        title.setForeground(LABEL); // Set text color

        JLabel sub = new JLabel("Manage your database connection"); // Subtitle label
        sub.setFont(sf(Font.PLAIN, 12f)); // Small font
        sub.setForeground(LABEL_2); // Secondary text color

        left.add(title); // Add title
        left.add(Box.createVerticalStrut(2)); // Add spacing
        left.add(sub); // Add subtitle

        header.add(left, BorderLayout.WEST); // Add left section to header
        return header; // Return completed header
    }

    // ════════════════════════════════════════════════════════════════════════
    // FORM CARD
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildForm() { // Builds main settings form
        JPanel wrapper = new JPanel(new BorderLayout()); // Wrapper panel
        wrapper.setOpaque(false); // Transparent

        JPanel card = createCard(); // Create card UI
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS)); // Vertical layout
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(SEP_LIGHT, R), // Rounded border
            BorderFactory.createEmptyBorder(28, 28, 28, 28) // Inner padding
        ));

        JLabel sectionTitle = new JLabel("Database Connection"); // Section title
        sectionTitle.setFont(sf(Font.BOLD, 15f)); // Bold font
        sectionTitle.setForeground(LABEL); // Text color
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        card.add(sectionTitle); // Add title

        card.add(Box.createVerticalStrut(4)); // Small spacing

        JLabel hint = new JLabel("Changes take effect immediately after saving."); // Hint text
        hint.setFont(sf(Font.PLAIN, 12f)); // Font style
        hint.setForeground(LABEL_3); // Light text color
        hint.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        card.add(hint); // Add hint

        card.add(Box.createVerticalStrut(22)); // Vertical spacing
        card.add(hairline()); // Add separator line
        card.add(Box.createVerticalStrut(22)); // More spacing

        // ── JDBC URL ─────────────────────────────────────────────────────────
        card.add(fieldLabel("JDBC URL")); // Add field label
        card.add(Box.createVerticalStrut(6)); // Spacing
        urlField = styledField(DEFAULT_URL); // Create styled URL field
        card.add(urlField); // Add URL field

        card.add(Box.createVerticalStrut(4)); // Small spacing

        JLabel urlHint = new JLabel(
            "  MySQL: jdbc:mysql://HOST:PORT/DB  |  Oracle: jdbc:oracle:thin:@HOST:PORT:SID"
        ); // Example JDBC formats

        urlHint.setFont(sf(Font.PLAIN, 11f)); // Small font
        urlHint.setForeground(LABEL_3); // Tertiary color
        urlHint.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        card.add(urlHint); // Add URL hint
        card.add(Box.createVerticalStrut(16)); // Spacing

        // ── Username ─────────────────────────────────────────────────────────
        card.add(fieldLabel("Username")); // Add username label
        card.add(Box.createVerticalStrut(6)); // Spacing
        userField = styledField(DEFAULT_USER); // Create username field
        card.add(userField); // Add field
        card.add(Box.createVerticalStrut(16)); // Spacing

        // ── Password ─────────────────────────────────────────────────────────
        card.add(fieldLabel("Password")); // Add password label
        card.add(Box.createVerticalStrut(6)); // Spacing

        passField = new JPasswordField(); // Create password field
        passField.setFont(sf(Font.PLAIN, 13f)); // Font style
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36)); // Set height
        passField.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        passField.setBackground(new Color(0xF9F9FB)); // Background color

        passField.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(SEP, 8), // Rounded border
            BorderFactory.createEmptyBorder(6, 10, 6, 10) // Inner padding
        ));

        card.add(passField); // Add password field
        card.add(Box.createVerticalStrut(28)); // Large spacing

        // ── Status indicator ─────────────────────────────────────────────────
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); // Status row
        statusRow.setOpaque(false); // Transparent
        statusRow.setAlignmentX(Component.LEFT_ALIGNMENT); // Align left
        statusRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24)); // Set max height

        statusDot = new JPanel() { // Custom painted dot
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create(); // Create graphics copy
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Smooth drawing
                g2.setColor(getBackground()); // Dot color
                g2.fillOval(0, 3, 10, 10); // Draw circle
                g2.dispose(); // Release graphics
            }

            @Override public Dimension getPreferredSize() {
                return new Dimension(10, 16); // Dot size
            }
        };

        statusDot.setOpaque(false); // Transparent background
        statusDot.setBackground(LABEL_3); // Default grey

        statusLabel = new JLabel("Not tested yet"); // Default status text
        statusLabel.setFont(sf(Font.PLAIN, 12f)); // Font style
        statusLabel.setForeground(LABEL_2); // Text color

        statusRow.add(statusDot); // Add dot
        statusRow.add(statusLabel); // Add label
        card.add(statusRow); // Add row to card

        wrapper.add(card, BorderLayout.CENTER); // Add card to wrapper
        return wrapper; // Return the form panel
    }

    // ════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ════════════════════════════════════════════════════════════════════════

    private JPanel createCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Soft shadow layers
                for (int i = 5; i >= 1; i--) {
                    g2.setColor(new Color(0, 0, 0, 5));
                    g2.fillRoundRect(i, i + 1,
                                     getWidth() - i * 2, getHeight() - i * 2,
                                     R + i, R + i);
                }
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, R, R);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    private JComponent hairline() {
        JPanel line = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SEP_LIGHT);
                g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
                g2.dispose();
            }
        };
        line.setOpaque(false);
        line.setPreferredSize(new Dimension(0, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return line;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(sf(Font.BOLD, 12f));
        label.setForeground(LABEL);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField styledField(String defaultText) {
        JTextField field = new JTextField(defaultText);
        field.setFont(sf(Font.PLAIN, 13f));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBackground(new Color(0xF9F9FB));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(SEP, 8),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    private void loadSavedSettings() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(PROPS_FILE)) {
            props.load(fis);
            String url = props.getProperty("db.url", DEFAULT_URL);
            String user = props.getProperty("db.user", DEFAULT_USER);
            String pass = props.getProperty("db.password", DEFAULT_PASS);
            urlField.setText(url);
            userField.setText(user);
            passField.setText(pass);
        } catch (IOException e) {
            // Use defaults if file not found or error
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // ROUND BORDER CLASS
    // ════════════════════════════════════════════════════════════════════════
    static class RoundBorder extends AbstractBorder {

        private final Color color;
        private final int   radius;

        RoundBorder(Color color, int radius) {
            this.color  = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.draw(new RoundRectangle2D.Double(x + .5, y + .5, w - 1, h - 1, radius, radius));
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c)           { return new Insets(1,1,1,1); }
        @Override public Insets getBorderInsets(Component c, Insets i) { i.set(1,1,1,1); return i; }
    }
}