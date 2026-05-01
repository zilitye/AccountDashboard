package com.ex.calculate;

import chart.SQLConnection;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;

/**
 * DatabasePage — shows every row in the expenses table as a live JTable.
 *
 * Features:
 *   • Refresh button — re-queries the DB
 *   • Delete selected row button — removes the row from the DB and table
 *   • Row count badge in the header
 *   • Styled to match the macOS Sonoma design language of the main app
 */
public class DatabasePage extends JPanel {

    // ── colour / font tokens (mirrors ExpensesComputeApplication) ────────────
    private static final Color BG        = new Color(0xf6f6f6);
    private static final Color CARD_BG   = Color.WHITE;
    private static final Color SEP       = new Color(0xC6C6C8);
    private static final Color SEP_LIGHT = new Color(0xE5E5EA);
    private static final Color LABEL     = new Color(0x1C1C1E);
    private static final Color LABEL_2   = new Color(0x6E6E73);
    private static final Color LABEL_3   = new Color(0xAEAEB2);
    private static final Color ACCENT    = new Color(0x007AFF);
    private static final Color RED       = new Color(0xFF3B30);
    private static final Color GREEN     = new Color(0x34C759);
    private static final Color STRIPE    = new Color(0xF2F2F7);   // alternating row tint
    private static final int   R         = 10;

    private static Font sf(int style, float size) {
        for (String n : new String[]{".SF NS Display", ".SF NS Text", "Helvetica Neue", "Helvetica", "SansSerif"}) {
            Font f = new Font(n, style, (int) size);
            if (!f.getFamily().equals("Dialog")) return f.deriveFont(size);
        }
        return new Font("SansSerif", style, (int) size);
    }

    // ── state ─────────────────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            table;
    private JLabel            rowCountLabel;

    // ─────────────────────────────────────────────────────────────────────────
    public DatabasePage() {
        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
        loadData();
    }

    // ════════════════════════════════════════════════════════════════════════
    // HEADER ROW  (title + row-count + buttons)
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        // Left: title + subtitle
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

        // Right: Refresh + Delete buttons
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton refreshBtn = buildBtn("↺  Refresh", ACCENT, Color.WHITE);
        refreshBtn.addActionListener(e -> loadData());

        JButton deleteBtn = buildBtn("⌫  Delete Row", new Color(0xFFE5E4), RED);
        deleteBtn.addActionListener(e -> deleteSelectedRow());

        right.add(deleteBtn);
        right.add(refreshBtn);

        header.add(left,  BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ════════════════════════════════════════════════════════════════════════
    // TABLE PANEL
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildTable() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout());

        // Column definitions — matches expenses table schema
        String[] cols = {"ID", "Year", "Month", "Category", "Amount (RM)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 4 ? Double.class : (c <= 2 ? Integer.class : String.class);
            }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) {
                    c.setBackground(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 30));
                    c.setForeground(LABEL);
                } else {
                    c.setBackground(row % 2 == 0 ? CARD_BG : STRIPE);
                    c.setForeground(LABEL);
                }
                if (c instanceof JLabel) ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return c;
            }
        };

        // Appearance
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

        // Column widths
        int[] widths = {55, 65, 75, 220, 120};
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Right-align the Amount column
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (v instanceof Double) setText(String.format("%.2f", (Double) v));
                setHorizontalAlignment(JLabel.RIGHT);
                return this;
            }
        };
        table.getColumnModel().getColumn(4).setCellRenderer(rightAlign);

        // Header styling
        JTableHeader th = table.getTableHeader();
        th.setFont(sf(Font.BOLD, 11.5f));
        th.setBackground(new Color(0xF2F2F7));
        th.setForeground(LABEL_2);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SEP_LIGHT));
        th.setReorderingAllowed(false);
        th.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
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

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.setBackground(CARD_BG);
        sp.getViewport().setBackground(CARD_BG);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(sp.getVerticalScrollBar());

        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    // ════════════════════════════════════════════════════════════════════════
    // DATA OPERATIONS
    // ════════════════════════════════════════════════════════════════════════
    public void loadData() {
        tableModel.setRowCount(0);
        try (Connection conn = SQLConnection.getInstance().getConnection()) {
            String sql = "SELECT id, year, month, category, amount FROM expenses ORDER BY year DESC, month DESC, id DESC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            int count = 0;
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getInt("year"),
                    rs.getInt("month"),
                    rs.getString("category"),
                    rs.getDouble("amount")
                });
                count++;
            }
            rowCountLabel.setText(count + " record" + (count == 1 ? "" : "s") + " in database");
        } catch (SQLException e) {
            e.printStackTrace();
            rowCountLabel.setText("⚠  Could not load data — check DB connection");
            rowCountLabel.setForeground(RED);
        }
    }

    private void deleteSelectedRow() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Select a row to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete expense record ID " + id + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = SQLConnection.getInstance().getConnection()) {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM expenses WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
            loadData();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Delete failed:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    // SHARED HELPERS (copied style from main app)
    // ════════════════════════════════════════════════════════════════════════
    private JPanel createCard() {
        JPanel card = new JPanel(new BorderLayout()) {
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
        card.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(SEP_LIGHT, R),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
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
        return btn;
    }

    private void styleScrollBar(JScrollBar bar) {
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0xC6C6C8);
                trackColor = CARD_BG;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroBtn(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroBtn(); }
            JButton zeroBtn() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }

    static class RoundBorder extends AbstractBorder {
        private final Color color;
        private final int   radius;
        RoundBorder(Color c, int r) { color = c; radius = r; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
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
