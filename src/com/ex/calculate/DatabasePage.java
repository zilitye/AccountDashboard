package com.ex.calculate;

import chart.SQLConnection;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabasePage — displays all rows from the expenses table in a styled JTable.
 *
 * Features:
 *   • Refresh button  — re-queries the database asynchronously
 *   • Delete button   — removes the selected row from the DB, then reloads
 *   • Row-count badge in the header
 *   • macOS Sonoma-inspired design language
 *
 * Bug fixed: deleteSelectedRow() no longer wraps the shared singleton
 * Connection in a try-with-resources block, which previously closed the
 * connection before loadData() could use it.
 */
public class DatabasePage extends JPanel {

    // ── Design tokens ────────────────────────────────────────────────────────
    private static final Color BG         = new Color(0xF6F6F6);
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color SEP_LIGHT  = new Color(0xE5E5EA);
    private static final Color LABEL      = new Color(0x1C1C1E);
    private static final Color LABEL_2    = new Color(0x6E6E73);
    private static final Color STRIPE     = new Color(0xF2F2F7);
    private static final Color ACCENT     = new Color(0x007AFF);
    private static final Color RED        = new Color(0xFF3B30);
    private static final Color RED_LIGHT  = new Color(0xFFE5E4);
    private static final int   RADIUS     = 10;

    // ── State ────────────────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            table;
    private JLabel            rowCountLabel;

    // ── Constructor ──────────────────────────────────────────────────────────
    public DatabasePage() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadData();
    }

    // ════════════════════════════════════════════════════════════════════════
    // UI BUILDERS
    // ════════════════════════════════════════════════════════════════════════

    /** Header row: title + row-count on the left; Delete + Refresh on the right. */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        // Left — title + subtitle
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("Database");
        title.setFont(sf(Font.BOLD, 20f));
        title.setForeground(LABEL);

        rowCountLabel = new JLabel("Loading…");
        rowCountLabel.setFont(sf(Font.PLAIN, 12f));
        rowCountLabel.setForeground(LABEL_2);

        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(rowCountLabel);

        // Right — action buttons
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton deleteBtn  = buildButton("⌫  Delete Row", RED_LIGHT, RED);
        JButton refreshBtn = buildButton("↺  Refresh",   ACCENT,     Color.WHITE);

        deleteBtn.addActionListener(e  -> deleteSelectedRow());
        refreshBtn.addActionListener(e -> loadData());

        right.add(deleteBtn);
        right.add(refreshBtn);

        header.add(left,  BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    /** Rounded card containing the scrollable JTable. */
    private JPanel buildTablePanel() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());

        // ── Table model ──────────────────────────────────────────────────────
        String[] columns = {"ID", "Year", "Month", "Category", "Amount (RM)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 4)       return Double.class;
                if (col <= 2)       return Integer.class;
                return String.class;
            }
        };

        // ── JTable ───────────────────────────────────────────────────────────
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,
                                             int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(ACCENT.getRed(), ACCENT.getGreen(),
                                              ACCENT.getBlue(), 30));
                } else {
                    c.setBackground(row % 2 == 0 ? CARD_BG : STRIPE);
                }
                c.setForeground(LABEL);
                if (c instanceof JLabel lbl) {
                    lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                }
                return c;
            }
        };

        table.setFont(sf(Font.PLAIN, 13f));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(CARD_BG);
        table.setForeground(LABEL);
        table.setSelectionBackground(new Color(0x007AFF22, true));
        table.setSelectionForeground(LABEL);
        table.setFocusable(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // ── Column widths ────────────────────────────────────────────────────
        int[] widths = {55, 65, 75, 220, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // ── Cell renderers ───────────────────────────────────────────────────
        DefaultTableCellRenderer leftAlign = new DefaultTableCellRenderer();
        leftAlign.setHorizontalAlignment(JLabel.LEFT);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(leftAlign);
        }

        // Right-align Amount column with 2 decimal places
        table.getColumnModel().getColumn(4).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                    if (v instanceof Double d) v = String.format("%.2f", d);
                    super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                    setHorizontalAlignment(JLabel.RIGHT);
                    return this;
                }
            }
        );

        // ── Table header ─────────────────────────────────────────────────────
        JTableHeader header = table.getTableHeader();
        header.setFont(sf(Font.BOLD, 11.5f));
        header.setBackground(new Color(0xF2F2F7));
        header.setForeground(LABEL_2);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SEP_LIGHT));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                lbl.setFont(sf(Font.BOLD, 11.5f));
                lbl.setForeground(LABEL_2);
                lbl.setBackground(new Color(0xF2F2F7));
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, SEP_LIGHT),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                return lbl;
            }
        });

        // ── Scroll pane ──────────────────────────────────────────────────────
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.setBackground(CARD_BG);
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(scrollPane.getVerticalScrollBar());

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    // DATA OPERATIONS
    // ════════════════════════════════════════════════════════════════════════

    /** Asynchronously fetches all rows from the expenses table and populates the model. */
    public void loadData() {
        tableModel.setRowCount(0);
        rowCountLabel.setText("Loading…");
        rowCountLabel.setForeground(LABEL_2);

        new SwingWorker<List<Object[]>, Void>() {

            private String statusText  = "";
            private Color  statusColor = LABEL_2;

            @Override
            protected List<Object[]> doInBackground() {
                List<Object[]> rows = new ArrayList<>();
                Connection conn = SQLConnection.getInstance().getConnection();

                if (conn == null) {
                    statusText  = "⚠  Database offline — check Settings";
                    statusColor = RED;
                    return rows;
                }

                String sql = "SELECT id, year, month, category, amount " +
                             "FROM expenses " +
                             "ORDER BY year DESC, month DESC, id DESC";

                try (PreparedStatement ps = conn.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        rows.add(new Object[]{
                            rs.getInt("id"),
                            rs.getInt("year"),
                            rs.getInt("month"),
                            rs.getString("category"),
                            rs.getDouble("amount")
                        });
                    }

                    int n   = rows.size();
                    statusText  = n + " record" + (n == 1 ? "" : "s") + " in database";
                    statusColor = LABEL_2;

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    statusText  = "⚠  Could not load data — check DB connection";
                    statusColor = RED;
                }

                return rows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    tableModel.setRowCount(0);
                    for (Object[] row : rows) tableModel.addRow(row);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                rowCountLabel.setText(statusText);
                rowCountLabel.setForeground(statusColor);
            }

        }.execute();
    }

    /**
     * Deletes the currently selected row from the database, then reloads the table.
     *
     * FIX: The shared singleton Connection is NOT wrapped in a try-with-resources
     * block. Doing so would call conn.close() on the shared instance, breaking all
     * subsequent queries (including the loadData() call below). Only the
     * PreparedStatement — which we own — is closed explicitly.
     */
    private void deleteSelectedRow() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a row to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete expense record ID " + id + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        // ✅ Retrieve the shared connection without closing it afterward.
        Connection conn = SQLConnection.getInstance().getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this,
                "Cannot delete: database is currently offline.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM expenses WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
            // Only the PreparedStatement is closed by try-with-resources — not conn.
            loadData();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Delete failed:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // STYLE HELPERS
    // ════════════════════════════════════════════════════════════════════════

    /** Resolves the best available San Francisco / Helvetica font on the system. */
    private static Font sf(int style, float size) {
        for (String name : new String[]{
                ".SF NS Display", ".SF NS Text", "Helvetica Neue", "Helvetica", "SansSerif"}) {
            Font f = new Font(name, style, (int) size);
            if (!f.getFamily().equals("Dialog")) return f.deriveFont(size);
        }
        return new Font("SansSerif", style, (int) size);
    }

    /** Rounded white card with a subtle drop shadow. */
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
                                     RADIUS + i, RADIUS + i);
                }
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS, RADIUS);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(SEP_LIGHT, RADIUS),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        return card;
    }

    /** Creates a pill-style button with custom background and foreground colours. */
    private JButton buildButton(String text, Color background, Color foreground) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed() ? background.darker() : background);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setFont(getFont());
                g2.setColor(foreground);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
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
        return btn;
    }

    /** Applies a minimal macOS-style appearance to a JScrollBar. */
    private void styleScrollBar(JScrollBar bar) {
        bar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(0xC6C6C8);
                trackColor = CARD_BG;
            }

            @Override protected JButton createDecreaseButton(int o) { return invisibleButton(); }
            @Override protected JButton createIncreaseButton(int o) { return invisibleButton(); }

            private JButton invisibleButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    // ════════════════════════════════════════════════════════════════════════
    // INNER CLASSES
    // ════════════════════════════════════════════════════════════════════════

    /** Thin rounded-rectangle border used on card panels. */
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
