package com.ex.calculate; // Package name for organizing classes

import chart.SQLConnection; // Import database connection helper class

import javax.swing.*; // Import Swing GUI components
import javax.swing.border.AbstractBorder; // Import custom border support
import javax.swing.plaf.basic.BasicScrollBarUI; // Import custom scrollbar UI
import javax.swing.table.DefaultTableCellRenderer; // Import table cell renderer
import javax.swing.table.DefaultTableModel; // Import table model
import javax.swing.table.JTableHeader; // Import table header
import java.awt.*; // Import AWT graphics and layout classes
import java.awt.geom.RoundRectangle2D; // Import rounded rectangle shape
import java.sql.Connection; // Import SQL connection
import java.sql.PreparedStatement; // Import SQL prepared statement
import java.sql.ResultSet; // Import SQL result set
import java.sql.SQLException; // Import SQL exception
import java.util.ArrayList; // Import ArrayList
import java.util.List; // Import List interface

public class DatabasePage extends JPanel { // Database page class extending JPanel

    // ===== Colors and Design Settings =====

    private static final Color BG = new Color(0xF6F6F6); // Background color
    private static final Color CARD_BG = Color.WHITE; // Card background color
    private static final Color SEP_LIGHT = new Color(0xE5E5EA); // Border color
    private static final Color LABEL = new Color(0x1C1C1E); // Main text color
    private static final Color LABEL_2 = new Color(0x6E6E73); // Secondary text color
    private static final Color STRIPE = new Color(0xF2F2F7); // Alternate row color
    private static final Color ACCENT = new Color(0x007AFF); // Blue accent color
    private static final Color RED = new Color(0xFF3B30); // Red color
    private static final Color RED_LIGHT = new Color(0xFFE5E4); // Light red color
    private static final int RADIUS = 10; // Rounded corner radius

    // ===== Variables =====

    private DefaultTableModel tableModel; // Table data model
    private JTable table; // JTable object
    private JLabel rowCountLabel; // Label showing total records

    // ===== Constructor =====

    public DatabasePage() {

        setLayout(new BorderLayout()); // Set layout manager
        setBackground(BG); // Set background color

        // Add outer padding
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        // Add header panel
        add(buildHeader(), BorderLayout.NORTH);

        // Add table panel
        add(buildTablePanel(), BorderLayout.CENTER);

        // Load database data
        loadData();
    }

    // ===== Create Header =====

    private JPanel buildHeader() {

        JPanel header = new JPanel(new BorderLayout()); // Header panel
        header.setOpaque(false); // Transparent background

        // Bottom spacing
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        // ===== Left Side =====

        JPanel left = new JPanel(); // Left container
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); // Vertical layout
        left.setOpaque(false);

        JLabel title = new JLabel("Database"); // Title label
        title.setFont(sf(Font.BOLD, 20f)); // Set font
        title.setForeground(LABEL); // Set text color

        rowCountLabel = new JLabel("Loading…"); // Record count label
        rowCountLabel.setFont(sf(Font.PLAIN, 12f)); // Set font
        rowCountLabel.setForeground(LABEL_2); // Set color

        left.add(title); // Add title
        left.add(Box.createVerticalStrut(2)); // Small spacing
        left.add(rowCountLabel); // Add row count label

        // ===== Right Side =====

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        // Create delete button
        JButton deleteBtn = buildButton("⌫  Delete Row", RED_LIGHT, RED);

        // Create refresh button
        JButton refreshBtn = buildButton("↺  Refresh", ACCENT, Color.WHITE);

        // Delete button action
        deleteBtn.addActionListener(e -> deleteSelectedRow());

        // Refresh button action
        refreshBtn.addActionListener(e -> loadData());

        // Add buttons
        right.add(deleteBtn);
        right.add(refreshBtn);

        // Add left and right sections
        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header; // Return completed header
    }

    // ===== Create Table Panel =====

    private JPanel buildTablePanel() {

        JPanel card = createCard(); // Create card panel
        card.setLayout(new BorderLayout());

        // ===== Table Model =====

        String[] columns = {
            "ID",
            "Year",
            "Month",
            "Category",
            "Amount (RM)"
        };

        // Create table model
        tableModel = new DefaultTableModel(columns, 0) {

            // Make table non-editable
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            // Define column data types
            @Override
            public Class<?> getColumnClass(int col) {

                if (col == 4)
                    return Double.class;

                if (col <= 2)
                    return Integer.class;

                return String.class;
            }
        };

        // ===== JTable =====

        table = new JTable(tableModel) {

            // Customize row appearance
            @Override
            public Component prepareRenderer(
                    javax.swing.table.TableCellRenderer renderer,
                    int row,
                    int col) {

                Component c = super.prepareRenderer(renderer, row, col);

                // Highlight selected row
                if (isRowSelected(row)) {

                    c.setBackground(new Color(
                            ACCENT.getRed(),
                            ACCENT.getGreen(),
                            ACCENT.getBlue(),
                            30));

                } else {

                    // Alternate row colors
                    c.setBackground(row % 2 == 0 ? CARD_BG : STRIPE);
                }

                c.setForeground(LABEL); // Text color

                // Add cell padding
                if (c instanceof JLabel lbl) {
                    lbl.setBorder(
                            BorderFactory.createEmptyBorder(0, 12, 0, 12));
                }

                return c;
            }
        };

        // Table styling
        table.setFont(sf(Font.PLAIN, 13f));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(CARD_BG);
        table.setForeground(LABEL);
        table.setSelectionForeground(LABEL);
        table.setFocusable(true);

        // Disable auto resize
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // ===== Column Widths =====

        int[] widths = {55, 65, 75, 220, 120};

        for (int i = 0; i < widths.length; i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        // ===== Left Alignment =====

        DefaultTableCellRenderer leftAlign =
                new DefaultTableCellRenderer();

        leftAlign.setHorizontalAlignment(JLabel.LEFT);

        // Apply left alignment
        for (int i = 0; i < table.getColumnCount(); i++) {

            table.getColumnModel()
                    .getColumn(i)
                    .setCellRenderer(leftAlign);
        }

        // ===== Amount Column Renderer =====

        table.getColumnModel().getColumn(4).setCellRenderer(

                new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t,
                            Object v,
                            boolean sel,
                            boolean foc,
                            int r,
                            int c) {

                        // Format amount to 2 decimal places
                        if (v instanceof Double d) {
                            v = String.format("%.2f", d);
                        }

                        super.getTableCellRendererComponent(
                                t, v, sel, foc, r, c);

                        // Right align amount
                        setHorizontalAlignment(JLabel.RIGHT);

                        return this;
                    }
                });

        // ===== Table Header =====

        JTableHeader header = table.getTableHeader();

        header.setFont(sf(Font.BOLD, 11.5f));
        header.setBackground(new Color(0xF2F2F7));
        header.setForeground(LABEL_2);

        // Bottom border
        header.setBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0, SEP_LIGHT));

        // Prevent column moving
        header.setReorderingAllowed(false);

        // ===== Scroll Pane =====

        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setBorder(null);
        scrollPane.setBackground(CARD_BG);

        // Match viewport background
        scrollPane.getViewport().setBackground(CARD_BG);

        // Smooth scrolling
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Apply custom scrollbar style
        styleScrollBar(scrollPane.getVerticalScrollBar());

        // Add scrollpane into card
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    // ===== Load Data From Database =====

    public void loadData() {

        // Clear existing rows
        tableModel.setRowCount(0);

        // Show loading text
        rowCountLabel.setText("Loading…");

        // Background worker
        new SwingWorker<List<Object[]>, Void>() {

            private String statusText = "";
            private Color statusColor = LABEL_2;

            @Override
            protected List<Object[]> doInBackground() {

                List<Object[]> rows = new ArrayList<>();

                // Get database connection
                Connection conn =
                        SQLConnection.getInstance().getConnection();

                // Database offline
                if (conn == null) {

                    statusText =
                            "⚠ Database offline — check Settings";

                    statusColor = RED;

                    return rows;
                }

                // SQL query
                String sql =
                        "SELECT id, year, month, category, amount " +
                        "FROM expenses " +
                        "ORDER BY year DESC, month DESC, id DESC";

                try (

                        PreparedStatement ps =
                                conn.prepareStatement(sql);

                        ResultSet rs = ps.executeQuery()

                ) {

                    // Read rows
                    while (rs.next()) {

                        rows.add(new Object[]{

                                rs.getInt("id"),
                                rs.getInt("year"),
                                rs.getInt("month"),
                                rs.getString("category"),
                                rs.getDouble("amount")
                        });
                    }

                    // Update status text
                    int n = rows.size();

                    statusText =
                            n + " record" +
                            (n == 1 ? "" : "s") +
                            " in database";

                } catch (SQLException ex) {

                    ex.printStackTrace();

                    statusText =
                            "⚠ Could not load data";

                    statusColor = RED;
                }

                return rows;
            }

            @Override
            protected void done() {

                try {

                    List<Object[]> rows = get();

                    // Add rows into table
                    for (Object[] row : rows) {
                        tableModel.addRow(row);
                    }

                } catch (Exception ex) {

                    ex.printStackTrace();
                }

                // Update status label
                rowCountLabel.setText(statusText);
                rowCountLabel.setForeground(statusColor);
            }

        }.execute();
    }

    // ===== Delete Selected Row =====

    private void deleteSelectedRow() {

        int selectedRow = table.getSelectedRow();

        // No row selected
        if (selectedRow < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a row to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);

            return;
        }

        // Get selected row ID
        int id = (int) tableModel.getValueAt(selectedRow, 0);

        // Confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete expense record ID " + id + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        // Cancel delete
        if (confirm != JOptionPane.YES_OPTION)
            return;

        // Get database connection
        Connection conn =
                SQLConnection.getInstance().getConnection();

        // Database offline
        if (conn == null) {

            JOptionPane.showMessageDialog(
                    this,
                    "Database offline.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            return;
        }

        try (

                PreparedStatement ps =
                        conn.prepareStatement(
                                "DELETE FROM expenses WHERE id = ?")

        ) {

            // Set ID parameter
            ps.setInt(1, id);

            // Execute delete
            ps.executeUpdate();

            // Reload table data
            loadData();

        } catch (SQLException ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Delete failed:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== Font Helper =====

    private static Font sf(int style, float size) {

        // Try macOS fonts
        for (String name : new String[]{
                ".SF NS Display",
                ".SF NS Text",
                "Helvetica Neue",
                "Helvetica",
                "SansSerif"}) {

            Font f = new Font(name, style, (int) size);

            if (!f.getFamily().equals("Dialog")) {
                return f.deriveFont(size);
            }
        }

        // Fallback font
        return new Font("SansSerif", style, (int) size);
    }

    // ===== Create Card Panel =====

    private JPanel createCard() {

        JPanel card = new JPanel(new BorderLayout()) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw shadow
                for (int i = 5; i >= 1; i--) {

                    g2.setColor(new Color(0, 0, 0, 5));

                    g2.fillRoundRect(
                            i,
                            i + 1,
                            getWidth() - i * 2,
                            getHeight() - i * 2,
                            RADIUS + i,
                            RADIUS + i);
                }

                // Draw card background
                g2.setColor(CARD_BG);

                g2.fillRoundRect(
                        0,
                        0,
                        getWidth() - 1,
                        getHeight() - 1,
                        RADIUS,
                        RADIUS);

                g2.dispose();
            }
        };

        card.setOpaque(false);

        // Rounded border
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        new RoundBorder(SEP_LIGHT, RADIUS),
                        BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        return card;
    }

    // ===== Create Button =====

    private JButton buildButton(
            String text,
            Color background,
            Color foreground) {

        JButton btn = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // Darker color when pressed
                g2.setColor(
                        getModel().isPressed()
                                ? background.darker()
                                : background);

                // Draw rounded button
                g2.fillRoundRect(
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        8,
                        8);

                // Draw text
                g2.setFont(getFont());
                g2.setColor(foreground);

                FontMetrics fm = g2.getFontMetrics();

                g2.drawString(
                        getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent()
                                - fm.getDescent()) / 2);

                g2.dispose();
            }
        };

        btn.setFont(sf(Font.PLAIN, 13f));
        btn.setPreferredSize(new Dimension(130, 30));

        // Remove default Swing styling
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);

        // Hand cursor
        btn.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    // ===== Scrollbar Style =====

    private void styleScrollBar(JScrollBar bar) {

        bar.setUI(new BasicScrollBarUI() {

            @Override
            protected void configureScrollBarColors() {

                thumbColor = new Color(0xC6C6C8);
                trackColor = CARD_BG;
            }

            @Override
            protected JButton createDecreaseButton(int o) {
                return invisibleButton();
            }

            @Override
            protected JButton createIncreaseButton(int o) {
                return invisibleButton();
            }

            // Invisible scrollbar buttons
            private JButton invisibleButton() {

                JButton b = new JButton();

                b.setPreferredSize(new Dimension(0, 0));

                return b;
            }
        });
    }

    // ===== Custom Rounded Border =====

    static class RoundBorder extends AbstractBorder {

        private final Color color; // Border color
        private final int radius; // Corner radius

        // Constructor
        RoundBorder(Color color, int radius) {

            this.color = color;
            this.radius = radius;
        }

        // Draw border
        @Override
        public void paintBorder(
                Component c,
                Graphics g,
                int x,
                int y,
                int w,
                int h) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(color);

            // Draw rounded border
            g2.draw(new RoundRectangle2D.Double(
                    x + .5,
                    y + .5,
                    w - 1,
                    h - 1,
                    radius,
                    radius));

            g2.dispose();
        }

        // Border spacing
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }

        // Border spacing
        @Override
        public Insets getBorderInsets(Component c, Insets i) {

            i.set(1, 1, 1, 1);

            return i;
        }
    }
}